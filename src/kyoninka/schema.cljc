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
  [;; procedure(手続きテンプレート)
   {:db/ident :procedure/id :db/valueType :db.type/keyword :db/cardinality :db.cardinality/one :db/unique :db.unique/identity}
   {:db/ident :procedure/name :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :procedure/law :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :procedure/authority :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :procedure/window :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
   {:db/ident :procedure/fee-jpy :db/valueType :db.type/long :db/cardinality :db.cardinality/one}
   {:db/ident :procedure/standard-period-days :db/valueType :db.type/long :db/cardinality :db.cardinality/one}
   {:db/ident :procedure/valid-years :db/valueType :db.type/long :db/cardinality :db.cardinality/one}
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
