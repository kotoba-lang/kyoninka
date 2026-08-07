(ns kyoninka.schema
  "日本の許認可手続きを procedure-as-data で表すための Datomic 互換スキーマと
  状態機械の定義。ADR-2607141620 (com-junkawasaki/root)。

  設計原則:
  - 手続き(procedure)は不変の data。案件(case)は手続きに対する進行状態。
  - 手数料・標準処理期間・様式は改定されうる「標準値」なので、必ず :verify
    (未確認フラグ + 確認方法) を伴う。申請直前の実値確認は手続きの一部。
  - 提出・官庁接触・支払いを伴う遷移/ステップは :requires-human true。
    このライブラリは next-action を提案するだけで、実行はしない。
  - 法的論点(:legal-questions)は結論を出さず data として保持する。"
  )

(def schema
  "Datomic 互換の属性宣言。**実データが正、ここが写し。**

  以前ここは実データとずれていた（2026-08-06 実測）:
  `:procedure/fee-jpy` は宣言だけで一度も使われず（実データは `:procedure/fee`）、
  `:procedure/standard-period-days` は `long` 宣言なのに実データは
  `{:value n :verify {...}}` の map で、`:procedure/{fee,documents,requirements,
  steps,legal-questions,waste-categories}` と `:document/* :requirement/* :step/*
  :question/*` の 4 族はまるごと未宣言だった。

  **ずれた理由は、この vector をどこも参照していなかったから。** 宣言が
  誰にも検査されなければ、実装が動いても宣言だけが古くなる —— しかも
  『Datomic 互換 schema がある』と読めるので、外からは気付けない。
  `schema_test.cljc` が宣言と実データの双方向一致を検査する（片方向だと
  未使用の宣言が残り続ける）。

  ## 複合値の扱い

  `:verify` を伴う標準値（手数料・標準処理期間）は **queryable なスカラと
  検証フラグに分けて**宣言する。map のまま 1 属性にすると『¥19,000 の手続きを
  探す』が書けない —— この library の目的は procedure-as-data なので、
  値が query から見えないなら data として持っている意味が薄い。

  ネストした集合（steps / documents / requirements / legal-questions）は
  それぞれ独立した entity 族なので `:db.type/ref` の cardinality/many。"
  [;; procedure(手続きテンプレート)
   {:db/ident :procedure/id :db/valueType :db.type/keyword :db/cardinality :db.cardinality/one :db/unique :db.unique/identity}
   {:db/ident :procedure/name :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :procedure/law :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :procedure/authority :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :procedure/window :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   ;; **法域軸。** 以前は属性が 1 つも無く、日本であることが暗黙だった。
   ;; ISO 3166-1 alpha-3。国より細かい単位（都道府県・州）は :procedure/window が
   ;; 持つ —— そちらは窓口であって法域ではないので、混ぜない。
   {:db/ident :procedure/jurisdiction :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   ;; **通貨中立の手数料。** `:procedure/fee-jpy` は型に JPY を焼いていたので、
   ;; 日本以外の手続きを収録した瞬間に嘘になる。金額と通貨を分ける。
   {:db/ident :procedure/fee-amount :db/valueType :db.type/long :db/cardinality :db.cardinality/one}
   {:db/ident :procedure/fee-currency :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :procedure/fee-kind :db/valueType :db.type/keyword :db/cardinality :db.cardinality/one}
   {:db/ident :procedure/fee-verify :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :procedure/standard-period-days :db/valueType :db.type/long :db/cardinality :db.cardinality/one}
   {:db/ident :procedure/standard-period-verify :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   ;; 有効期間。**nil を 0 にしない** —— 古物商許可のように更新制度が無い手続きが
   ;; あり、0 年と「期限なし」は別のこと。属性を出さないことで表す。
   {:db/ident :procedure/valid-years :db/valueType :db.type/long :db/cardinality :db.cardinality/one}
   {:db/ident :procedure/requirements :db/valueType :db.type/ref :db/cardinality :db.cardinality/many}
   {:db/ident :procedure/steps :db/valueType :db.type/ref :db/cardinality :db.cardinality/many}
   {:db/ident :procedure/documents :db/valueType :db.type/ref :db/cardinality :db.cardinality/many}
   {:db/ident :procedure/legal-questions :db/valueType :db.type/ref :db/cardinality :db.cardinality/many}
   ;; 手続き固有の分類。**2 つの手続きが別の属性名を使っている**（実測）:
   ;; sanpai は `:procedure/waste-categories`（産廃の品目）、kobutsu は
   ;; `:procedure/categories`（古物の区分）。同じ概念だが語彙が違うので統合せず
   ;; 両方宣言する —— 片方に寄せると、その手続きの用語ではない名前で保持することになる。
   {:db/ident :procedure/waste-categories :db/valueType :db.type/keyword :db/cardinality :db.cardinality/many}
   {:db/ident :procedure/categories :db/valueType :db.type/keyword :db/cardinality :db.cardinality/many}
   ;; 複合値そのもの。datom 面へは下の 4 スカラに展開されるが、**library の
   ;; 実データはこの map で持つ**（`:verify` を値から離さないため）。
   {:db/ident :procedure/fee :db/valueType :db.type/ref :db/cardinality :db.cardinality/one}
   ;; requirement(許可要件)
   {:db/ident :requirement/id :db/valueType :db.type/keyword :db/cardinality :db.cardinality/one :db/unique :db.unique/identity}
   {:db/ident :requirement/name :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :requirement/detail :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   ;; 実データは `blocking?`（述語形）。**宣言側を実データに合わせる** ——
   ;; 逆をやると 1 つの概念に 2 つの名前ができる。
   {:db/ident :requirement/blocking? :db/valueType :db.type/boolean :db/cardinality :db.cardinality/one}
   ;; step(手順)
   {:db/ident :step/id :db/valueType :db.type/keyword :db/cardinality :db.cardinality/one :db/unique :db.unique/identity}
   {:db/ident :step/order :db/valueType :db.type/long :db/cardinality :db.cardinality/one}
   {:db/ident :step/title :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :step/detail :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   ;; 提出・官庁接触・支払いを伴う step。**この library は提案するだけで実行しない。**
   {:db/ident :step/requires-human :db/valueType :db.type/boolean :db/cardinality :db.cardinality/one}
   {:db/ident :step/errand :db/valueType :db.type/keyword :db/cardinality :db.cardinality/one}
   ;; document(必要書類)
   {:db/ident :document/id :db/valueType :db.type/keyword :db/cardinality :db.cardinality/one :db/unique :db.unique/identity}
   {:db/ident :document/name :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :document/who :db/valueType :db.type/keyword :db/cardinality :db.cardinality/one}
   ;; legal-question(法的論点)。**結論を出さず data として保持する。**
   {:db/ident :question/id :db/valueType :db.type/keyword :db/cardinality :db.cardinality/one :db/unique :db.unique/identity}
   {:db/ident :question/title :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :question/detail :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :question/status :db/valueType :db.type/keyword :db/cardinality :db.cardinality/one}
   ;; case(案件)
   {:db/ident :case/id :db/valueType :db.type/keyword :db/cardinality :db.cardinality/one :db/unique :db.unique/identity}
   {:db/ident :case/procedure :db/valueType :db.type/ref :db/cardinality :db.cardinality/one}
   {:db/ident :case/applicant :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :case/status :db/valueType :db.type/keyword :db/cardinality :db.cardinality/one}
   {:db/ident :case/done-steps :db/valueType :db.type/keyword :db/cardinality :db.cardinality/many}
   {:db/ident :case/collected-docs :db/valueType :db.type/keyword :db/cardinality :db.cardinality/many}])

(def statuses
  [:not-started :preparing :ready-to-submit :submitted :under-review
   :granted :rejected :withdrawn])

(def transitions
  "有効な状態遷移。:human のものは human-approved な event でしか進めない
  (提出・取り下げ・官庁の処分の記録は必ず人間の確認を経る)。"
  {:not-started     {:preparing :auto}
   :preparing       {:ready-to-submit :auto}
   :ready-to-submit {:submitted :human
                     :preparing :auto}
   :submitted       {:under-review :auto
                     :withdrawn :human}
   :under-review    {:granted :human
                     :rejected :human
                     :withdrawn :human}})

(defn transition-kind
  "from → to が有効なら :auto / :human、無効なら nil。"
  [from to]
  (get-in transitions [from to]))

(defn unverified
  "標準値に付ける『申請前に実値確認せよ』フラグ。"
  [how]
  {:status :unverified
   :how how
   :note "行政の手数料・様式・処理期間は改定されうる。申請直前に必ず最新値を確認する(ADR-2607141620 捏造ゼロ原則)。"})
