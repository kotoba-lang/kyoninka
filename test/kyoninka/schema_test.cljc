(ns kyoninka.schema-test
  "`schema/schema` が**実データと一致していること**の検査。

  ## なぜこれが要るのか

  2026-08-06 の実測で、宣言と実データが 4 点でずれていた:

  - `:procedure/fee-jpy` は宣言だけで一度も使われていない（実データは `:procedure/fee`）
  - `:procedure/standard-period-days` は `long` 宣言なのに実データは
    `{:value n :verify {...}}` の map
  - `:procedure/{fee,documents,requirements,steps,legal-questions,waste-categories}` と
    `:document/* :requirement/* :step/* :question/*` の 4 族がまるごと未宣言
  - 法域属性が 1 つも無く、日本であることが暗黙だった

  **ずれた理由は、`schema/schema` をどこも参照していなかったから。**
  実装が動いていても宣言だけが古くなり、しかも外からは『Datomic 互換 schema が
  ある』と読めるので気付けない。

  ## 双方向に検査する

  片方向（データ ⊆ 宣言）だけだと、使われなくなった宣言が残り続ける ——
  それがまさに `:procedure/fee-jpy` の状態だった。逆方向（宣言 ⊆ データ）も
  見る。ただし `:case/*` は**実行時にしか現れない**（このライブラリは case の
  実データを持たない）ので、そこだけ明示的に除外する。除外を暗黙にせず
  名指しで書くのは、次に足す人が「なぜここだけ緩いのか」を読めるようにするため。"
  (:require [clojure.test :refer [deftest is testing]]
            [kyoninka.schema :as schema]
            [kyoninka.sanpai :as sanpai]
            [kyoninka.kobutsu :as kobutsu]))

(def declared
  (into #{} (map :db/ident) schema/schema))

(def procedures [sanpai/procedure kobutsu/procedure])

(defn- attrs-of
  "map の中に現れる **namespace 付き keyword キー**を再帰的に集める。
  `:verify` の中の `:status`/`:how`/`:note` のような裸のキーは属性ではないので
  拾わない（属性は必ず namespace を持つ、というのがこの schema の約束）。"
  [x]
  (cond
    (map? x) (into (into #{} (filter qualified-keyword?) (keys x))
                   (mapcat attrs-of (vals x)))
    (sequential? x) (into #{} (mapcat attrs-of x))
    :else #{}))

(def used (into #{} (mapcat attrs-of procedures)))

;; case は実行時にしか現れない（このライブラリは case の実データを持たない）。
(def runtime-only #{:case/id :case/procedure :case/applicant :case/status
                    :case/done-steps :case/collected-docs})

;; 複合値をスカラに割った分。実データ側は `:procedure/fee` の中の
;; `:amount`/`:currency` として持ち、datom 面ではこの 4 属性に展開される。
(def projected-from-composite
  #{:procedure/fee-amount :procedure/fee-currency :procedure/fee-kind
    :procedure/fee-verify :procedure/standard-period-verify})

(deftest every-used-attribute-is-declared
  (testing "実データに現れる属性は全部宣言されている"
    (let [missing (sort (remove declared used))]
      (is (empty? missing)
          (str "宣言されていない属性: " (pr-str missing)
               " —— schema.cljc に足すこと。実データが正で、宣言が写し。")))))

(deftest every-declared-attribute-is-used
  (testing "宣言されている属性は実データか射影で使われている（死んだ宣言を残さない）"
    (let [unused (sort (remove (into runtime-only projected-from-composite)
                               (remove used declared)))]
      (is (empty? unused)
          (str "誰も使っていない宣言: " (pr-str unused)
               " —— :procedure/fee-jpy がこの状態で放置されていた。"
               "使わないなら消す。実行時にしか現れないなら runtime-only に足す。")))))

(deftest jurisdiction-is-explicit
  (testing "法域が暗黙でない —— 収録は日本だけだが、それを属性として言う"
    (is (contains? declared :procedure/jurisdiction))
    (doseq [p procedures]
      (is (= "JPN" (:procedure/jurisdiction p))
          (str (:procedure/id p) " に法域が無い。"
               "日本以外を収録するとき、暗黙の JPN は静かに嘘になる。")))))

(deftest fee-is-currency-neutral
  (testing "手数料は通貨を型に焼かない"
    (is (not (contains? declared :procedure/fee-jpy))
        ":procedure/fee-jpy は通貨を属性名に持つので、日本以外を収録した瞬間に嘘になる")
    (doseq [p procedures
            :let [fee (:procedure/fee p)]]
      (is (number? (:amount fee)) (str (:procedure/id p) " の手数料に金額が無い"))
      (is (string? (:currency fee)) (str (:procedure/id p) " の手数料に通貨が無い")))))

(deftest standard-values-carry-verify
  (testing "改定されうる標準値は :verify を伴う（ADR-2607141620 の捏造ゼロ原則）"
    (doseq [p procedures]
      (is (some? (get-in p [:procedure/fee :verify]))
          (str (:procedure/id p) " の手数料に :verify が無い"))
      (is (some? (get-in p [:procedure/standard-period-days :verify]))
          (str (:procedure/id p) " の標準処理期間に :verify が無い")))))
