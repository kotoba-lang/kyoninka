(ns kyoninka.kobutsu
  "古物商許可の手続き data。営業所所在地: 東京都千代田区丸の内（Gftd Japan の
  本店所在地）→ 窓口は丸の内警察署テンプレート。ADR-2607141620。

  収録値は標準値であり :verify フラグ付き — 申請直前に管轄警察署で実値・様式を
  確認すること(それ自体が step 1)。"
  (:require [kyoninka.schema :as schema]))

(def procedure
  {:procedure/id :kobutsu-marunouchi
   :procedure/name "古物商許可（東京都公安委員会・営業所: 千代田区丸の内）"
   :procedure/law "古物営業法 第3条"
   ;; **法域。** 以前は暗黙に日本だった。古物商許可は都道府県公安委員会の許可（法域は日本、窓口が管轄警察署）。
   :procedure/jurisdiction "JPN"
   :procedure/authority "東京都公安委員会"
   :procedure/window "営業所所在地の管轄警察署 生活安全課 防犯係（丸の内警察署。事前電話予約が通例）"
   :procedure/fee {:amount 19000
                   ;; **通貨を型に焼かない。** 以前の schema は :procedure/fee-jpy で
                   ;; JPY を属性名に持っており、日本以外を収録した瞬間に嘘になった。
                   :currency "JPY"
                   ;; JPY は最小単位の端数を持たないので minor-unit は 1。
                   :minor-unit 1
                   :kind :新規許可申請手数料
                   :verify (schema/unverified "警視庁の公式ページ/管轄警察署で最新額を確認")}
   :procedure/standard-period-days {:value 40
                                    :verify (schema/unverified "警視庁公表の標準処理期間を確認")}
   :procedure/valid-years nil ; 古物商許可に有効期間・更新制度はない(廃止/変更届のみ)

   ;; ITAD で扱う古物区分
   :procedure/categories
   [{:id :office-equipment :name "事務機器類" :itad-note "PC・サーバー・周辺機器の買取リユース(主たる区分)"}
    {:id :machines :name "機械工具類" :itad-note "扱いによっては該当。窓口で区分の解釈を確認"}]

   :procedure/requirements
   [{:requirement/id :not-disqualified
     :requirement/name "欠格要件非該当"
     :requirement/detail "古物営業法4条（一定の刑・破産手続開始の決定を受けて復権を得ない者・暴力団関係・住居不定等）に、法人役員・管理者が該当しないこと。"
     :requirement/blocking? true}
    {:requirement/id :manager
     :requirement/name "管理者の選任"
     :requirement/detail "営業所ごとに、常勤の管理者を1名選任すること（役員兼任可）。"
     :requirement/blocking? true}
    {:requirement/id :office
     :requirement/name "営業所の実体"
     :requirement/detail "独立した営業所の実体（使用権原）。バーチャルオフィスは不可となる場合が多い — 丸の内オフィスの契約形態を窓口に確認。"
     :requirement/blocking? true}]

   :procedure/documents
   [{:document/id :application-form :document/name "許可申請書（別記様式第1号）" :document/who :company}
    {:document/id :teikan :document/name "定款の写し（奥書き）" :document/who :company}
    {:document/id :touki :document/name "履歴事項全部証明書（登記事項証明書）" :document/who :company}
    {:document/id :yakuin-juminhyo :document/name "役員全員+管理者の住民票（本籍記載・マイナンバー無し）" :document/who :officers}
    {:document/id :yakuin-mibun :document/name "役員全員+管理者の身分証明書（本籍地市区町村発行）" :document/who :officers}
    {:document/id :seiyakusho :document/name "役員全員+管理者の誓約書" :document/who :officers}
    {:document/id :ryakureki :document/name "役員全員+管理者の略歴書（直近5年）" :document/who :officers}
    {:document/id :url-somei :document/name "URL の使用権限を疎明する資料（Web で取引する場合。itad.gftd.ai / gftd.co.jp）" :document/who :company}
    {:document/id :office-right :document/name "営業所の使用権原を示す書類（賃貸借契約書等。窓口により要否が異なる）" :document/who :company}]

   :procedure/steps
   [{:step/id :verify-current-rules :step/order 1 :step/requires-human true
     :step/title "管轄警察署（丸の内署 防犯係）へ事前相談・最新様式と手数料の確認"
     :step/detail "所在地の管轄確認・営業所の実体要件（オフィス契約形態）・URL 届出の要領を確認する。"
     :step/errand {:kind :verify-authority-info :draft-via :tayori
                   :evidence-schema {:authority :string :verified :map
                                     :source :string :date :iso-date}}}
    {:step/id :collect-documents :step/order 2 :step/requires-human false
     :step/title "申請書類一式の収集（役員全員分の本籍地書類は取り寄せに時間がかかるので先行）"
     :step/errand {:kind :collect-documents :draft-via nil
                   :evidence-schema {:doc :keyword :obtained-date :iso-date}}}
    {:step/id :appoint-manager :step/order 3 :step/requires-human true
     :step/title "営業所管理者の選任"}
    {:step/id :submit :step/order 4 :step/requires-human true
     :step/title "警察署へ申請提出と手数料納付（事前予約）"}
    {:step/id :respond-to-review :step/order 5 :step/requires-human true
     :step/title "審査対応（追加資料・確認への応答）"}
    {:step/id :post-grant :step/order 6 :step/requires-human true
     :step/title "許可後の義務対応（標識掲示・古物台帳・URL 届出の完了）"}]

   :procedure/legal-questions
   [{:question/id :virtual-office
     :question/title "丸の内オフィスの営業所適格性"
     :question/detail "本店所在地（グラントウキョウサウスタワー11F）の契約形態がサービスオフィス/シェアオフィスの場合、営業所の独立性・使用権原の扱いを管轄署に確認する必要がある。"
     :question/status :open}]})
