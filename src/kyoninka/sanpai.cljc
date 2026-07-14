(ns kyoninka.sanpai
  "産業廃棄物収集運搬業許可(積替え保管を除く)の手続き data。
  東京都テンプレート。ADR-2607141620。

  収録値は一般に公表されている標準値であり、:verify フラグ付き —
  申請直前に許可権者の公式情報で実値を確認すること(それ自体が step 1)。"
  (:require [kyoninka.schema :as schema]))

(def procedure
  {:procedure/id :sanpai-shuun-tokyo
   :procedure/name "産業廃棄物収集運搬業許可（東京都・積替え保管なし）"
   :procedure/law "廃棄物の処理及び清掃に関する法律（廃棄物処理法）第14条第1項"
   :procedure/authority "東京都知事（環境局 資源循環推進部）"
   :procedure/window "東京都環境局（申請は郵送/窓口。最新の受付方法は要確認）"
   :procedure/fee {:amount-jpy 81000
                   :kind :新規許可申請手数料
                   :verify (schema/unverified "東京都環境局の公式ページ/手数料条例で最新額を確認")}
   :procedure/standard-period-days {:value 60
                                    :verify (schema/unverified "東京都環境局の標準処理期間の公表値を確認")}
   :procedure/valid-years 5

   ;; ITAD(PC 廃棄)で運搬する産業廃棄物の品目(事業計画に記載)
   :procedure/waste-categories
   [{:id :waste-plastics :name "廃プラスチック類" :itad-note "PC 筐体・ケーブル被覆"}
    {:id :metal-scrap :name "金属くず" :itad-note "シャーシ・HDD・電源ユニット"}
    {:id :glass-ceramic :name "ガラスくず・コンクリートくず及び陶磁器くず" :itad-note "ディスプレイパネル・基板ガラス"}]

   :procedure/requirements
   [{:requirement/id :jw-course
     :requirement/name "講習会修了"
     :requirement/detail "（公財）日本産業廃棄物処理振興センター（JWセンター）の産業廃棄物処理業講習会『収集・運搬課程（新規）』を、申請者（法人の場合は代表者・役員または政令使用人）が修了していること。修了証は申請書類。"
     :requirement/blocking? true}
    {:requirement/id :financial-basis
     :requirement/name "経理的基礎"
     :requirement/detail "事業を的確かつ継続して行うに足りる経理的基礎（債務超過でない等）。直前3年分の貸借対照表・損益計算書・法人税納税証明書で疎明する。"
     :requirement/blocking? true}
    {:requirement/id :not-disqualified
     :requirement/name "欠格要件非該当"
     :requirement/detail "法14条5項2号（法7条5項4号準用）の欠格要件（一定の刑・暴力団関係・許可取消歴等）に、法人・役員・政令使用人・5%以上株主が該当しないこと。"
     :requirement/blocking? true}
    {:requirement/id :business-plan
     :requirement/name "事業計画・運搬施設"
     :requirement/detail "運搬する産業廃棄物の種類と数量、運搬車両（車検証・写真）、運搬容器、駐車場の使用権原、飛散・流出防止措置を事業計画として示す。"
     :requirement/blocking? true}]

   :procedure/documents
   [{:document/id :application-form :document/name "許可申請書（都様式）" :document/who :company}
    {:document/id :business-plan-outline :document/name "事業計画の概要（品目・数量・運搬先）" :document/who :company}
    {:document/id :teikan :document/name "定款の写し" :document/who :company}
    {:document/id :touki :document/name "履歴事項全部証明書（登記事項証明書）" :document/who :company}
    {:document/id :yakuin-juminhyo :document/name "役員等の住民票（本籍記載・マイナンバー無し）" :document/who :officers}
    {:document/id :yakuin-seinen :document/name "役員等の登記されていないことの証明書" :document/who :officers}
    {:document/id :zaimu-3y :document/name "直前3年分の貸借対照表・損益計算書・株主資本等変動計算書" :document/who :company}
    {:document/id :nouzei :document/name "法人税の納税証明書（その1）直前3年分" :document/who :company}
    {:document/id :jw-certificate :document/name "JWセンター講習会修了証の写し" :document/who :officers}
    {:document/id :vehicle :document/name "運搬車両の車検証写し・車両写真" :document/who :company}
    {:document/id :containers :document/name "運搬容器の写真" :document/who :company}
    {:document/id :parking :document/name "駐車場の使用権原を示す書類（賃貸借契約書等）" :document/who :company}]

   :procedure/steps
   [{:step/id :verify-current-rules :step/order 1 :step/requires-human true
     :step/title "最新の手数料・様式・受付方法を東京都環境局で確認"
     :step/detail "収録値は標準値。申請の手引き最新版を取得し、様式改定・電子申請可否を確認する。"}
    {:step/id :resolve-legal-questions :step/order 2 :step/requires-human true
     :step/title "事業スキームの法的整理（行政書士相談）"
     :step/detail ":legal-questions を行政書士に諮り、宅配回収 ITAD で本許可が必要なスキームかを確定させる。"}
    {:step/id :book-jw-course :step/order 3 :step/requires-human true
     :step/title "JWセンター講習会（収集・運搬課程 新規）の受講予約と受講"
     :step/detail "受講者 = 代表者または役員。人気日程は埋まるため最優先で予約。修了試験合格まで。"}
    {:step/id :collect-documents :step/order 4 :step/requires-human false
     :step/title "申請書類一式の収集（:procedure/documents のチェックリスト消化）"}
    {:step/id :prepare-business-plan :step/order 5 :step/requires-human false
     :step/title "事業計画書の作成（品目・数量・車両・容器・駐車場）"}
    {:step/id :submit :step/order 6 :step/requires-human true
     :step/title "申請書提出と手数料納付"
     :step/detail "提出は human gate。控えと受付番号を台帳に記録する。"}
    {:step/id :respond-to-review :step/order 7 :step/requires-human true
     :step/title "審査対応（補正指示への応答）"}]

   ;; 結論を出さずに保持する法的論点(行政書士・弁護士確認事項)
   :procedure/legal-questions
   [{:question/id :haikibutsu-gaitousei
     :question/title "宅配回収 ITAD の廃棄物該当性"
     :question/detail "有償買取（リユース目的）は古物営業の範囲で廃棄物処理法の外になりうる一方、処分費を収受して引き取る PC は産業廃棄物。総合判断説（昭和52年通知系）の下でスキーム毎に判定が分かれる。"
     :question/status :open}
    {:question/id :unsou-itaku
     :question/title "宅配便（貨物運送事業者）による回収の扱い"
     :question/detail "排出事業者が運送事業者へ運送委託する形の宅配回収で、当社に収集運搬許可が必要か（自社は運搬せず処分委託・消去のみを担うスキームの可否）。"
     :question/status :open}
    {:question/id :kuiki
     :question/title "許可の地域範囲"
     :question/detail "収集運搬許可は積む場所・降ろす場所の都道府県ごとに必要。全国宅配回収の場合にどの都道府県の許可を取るか（または運送委託で回避するか）はスキーム確定後に決める。"
     :question/status :open}]})
