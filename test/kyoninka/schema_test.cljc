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
            [clojure.string :as str]
            [kyoninka.schema :as schema]
            [kyoninka.sanpai :as sanpai]
            [kyoninka.kobutsu :as kobutsu]
            [kyoninka.gbr-waste-carrier :as gbr-waste]
            [kyoninka.gbr-scrap-metal :as gbr-scrap]
            [kyoninka.deu-abfall-transport :as deu-abfall]))

(def declared
  (into #{} (map :db/ident) schema/schema))

(def procedures [sanpai/procedure kobutsu/procedure
                 gbr-waste/procedure gbr-scrap/procedure deu-abfall/procedure])

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
  ;; `:procedure/fee` の中の `:amount`/`:currency`/`:kind`/`:minor-unit`/`:verify` と
  ;; `:procedure/standard-period-days` の `:verify` は、datom 面ではこの名前の
  ;; スカラに展開される。library の実データは複合値のまま持つ（`:verify` を
  ;; 値から離さないため）。
  #{:procedure/fee-amount :procedure/fee-currency :procedure/fee-kind
    :procedure/fee-minor-unit :procedure/fee-verify :procedure/standard-period-verify})

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
  ;; **最初この検査は `(= \"JPN\" ...)` と書いていた。** 暗黙の JPN を止めるために
  ;; 書いた検査が、非 JPN を 1 本足した瞬間に自分で非 JPN を弾く —— 3 本を
  ;; 並行で書いた 3 者が独立にこれを指摘した（2026-08-07）。
  ;; 検査すべきは「法域が明示されていること」であって「日本であること」ではない。
  (testing "法域が明示され、ISO 3166-1 alpha-3 の形をしている"
    (is (contains? declared :procedure/jurisdiction))
    (doseq [p procedures
            :let [j (:procedure/jurisdiction p)]]
      (is (and (string? j) (re-matches #"[A-Z]{3}" j))
          (str (:procedure/id p) " の法域が ISO3 でない: " (pr-str j))))))

(deftest extent-is-declarable
  ;; **法域コードより狭い制度が実在する。** SMDA 2013 の extent は England and
  ;; Wales のみ、廃棄物運搬業登録は England のみで、どちらも "GBR" 全域を
  ;; 覆わない。窓口（`:procedure/window`）とは別の軸なので別属性で持つ。
  ;; 省略は「法域全域」を意味するので、**省略と未調査を区別できない**
  ;; ことは限界として認めた上で、少なくとも書ける場所は用意する。
  (testing ":procedure/extent が宣言されている"
    (is (contains? declared :procedure/extent))))

(deftest fee-is-currency-neutral
  ;; **最初この検査は `(number? (:amount fee))` を必須にしていた。** だが
  ;; 「額が手続きの外（窓口）で決まる」制度が実在する —— 英国のスクラップ金属
  ;; 免許は council が決め、実測で £181〜£804.78 と 4 倍以上開く。
  ;; 代表額を書けば残り全部で嘘になるので、**額を書かないことが正しい**。
  ;; したがって必須なのは通貨と最小単位で、額は「あるなら整数」。
  (testing "手数料は通貨を型に焼かない"
    (is (not (contains? declared :procedure/fee-jpy))
        ":procedure/fee-jpy は通貨を属性名に持つので、日本以外を収録した瞬間に嘘になる"))

  (doseq [p procedures
          :let [fee (:procedure/fee p)]
          :when fee]
    (testing (str (:procedure/id p) " の手数料")
      (is (string? (:currency fee)) "通貨が無い")
      (is (integer? (:minor-unit fee))
          "最小単位が無い（1 通貨単位あたりの最小単位数。GBP/EUR は 100、JPY は 1）")
      ;; **額は最小単位の整数。** 浮動小数で金額を持たない ——
      ;; £191.02 を 191.02 のまま持とうとして schema の long 宣言が破れた。
      (when (contains? fee :amount)
        (is (integer? (:amount fee))
            (str "額は最小単位の整数で持つこと（実際の値: " (pr-str (:amount fee)) "）")))
      ;; 額が無いなら、なぜ無いかが :verify に書かれていること。
      (when-not (contains? fee :amount)
        (is (some? (:verify fee))
            "額が無いのに :verify も無い —— 『調べていない』と『ここでは決まらない』が区別できない")))))

(deftest standard-values-carry-verify
  ;; **最初この検査は両方の存在を必須にしていた。** だが「法にも公表にも
  ;; 決定期限が無い」制度が実在する（England の廃棄物運搬業登録、
  ;; 英国スクラップ金属免許、ドイツの届出）。**無いものを 0 や推測で埋めさせない**
  ;; ために、必須なのは「あるなら :verify を伴う」ことだけにする。
  (testing "改定されうる標準値は、収録されているなら :verify を伴う（捏造ゼロ）"
    (doseq [p procedures]
      (when (contains? p :procedure/fee)
        (is (some? (get-in p [:procedure/fee :verify]))
            (str (:procedure/id p) " の手数料に :verify が無い")))
      (when (contains? p :procedure/standard-period-days)
        (is (some? (get-in p [:procedure/standard-period-days :verify]))
            (str (:procedure/id p) " の標準処理期間に :verify が無い"))))))

(deftest sources-are-queryable
  ;; この library の原則は「捏造ゼロ・すべての値に出所」。**出所が ns docstring に
  ;; しか無いと data として引けない**（3 本を並行で書いた 3 者が独立に指摘）。
  (testing ":procedure/source-urls が宣言され、非 JPN の 3 本は出所を data で持つ"
    (is (contains? declared :procedure/source-urls))
    (doseq [p [gbr-waste/procedure gbr-scrap/procedure deu-abfall/procedure]]
      (is (seq (:procedure/source-urls p))
          (str (:procedure/id p) " に出所 URL が無い"))
      (doseq [u (:procedure/source-urls p)]
        (is (str/starts-with? u "http") (str "URL でない出所: " (pr-str u)))))))
