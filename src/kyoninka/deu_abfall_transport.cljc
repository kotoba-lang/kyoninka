(ns kyoninka.deu-abfall-transport
  "ドイツの廃棄物の収集・運搬・取引・仲介（Sammeln / Befördern / Handeln / Makeln）の
  開業手続き data。連邦法テンプレート。ADR-2607141620。

  ## 日本の2本と構造が違う点（ここを取り違えると全部ずれる）

  1. **二段構造の分岐軸が『危険廃棄物かどうか』。** 日本は収集運搬と処分で分けるが、
     ドイツは扱う廃棄物が gefährlich か否かで **§53 の Anzeige（届出）** と
     **§54 の Erlaubnis（許可）** に分かれる。だから step 1 は書類集めでも窓口確認でも
     なく、**AVV による廃棄物分類の確定**である。
  2. **名宛人が広い。** Sammler・Beförderer だけでなく **Händler（取引）と Makler（仲介）**
     も対象。自ら 1 メートルも運ばず取り次ぐだけの ops 層がそのまま規制対象に入りうる。
  3. **法域は連邦、窓口は州。** KrWG / AbfAEV は連邦法だが、所管は
     『申請者が Hauptsitz を置く州の当局』（KrWG §53 Abs.1 S.3 / §54 Abs.1 S.3）。
     手数料は州の Gebührenordnung で決まり、州ごとに違う。
  4. **許可は全国有効・無期限が既定。** §54 Abs.1 S.4 で連邦全域に効く。日本の
     収集運搬業許可が積み下ろしの都道府県ごとに要るのと正反対。
  5. **沈黙が許可になる。** 完全な申請の到達から 3 か月で Genehmigungsfiktion が働き、
     当局が動かなくても許可されたものとみなされる（§54 Abs.6 S.2 KrWG i.V.m.
     §42a VwVfG、AbfAEV §10 Abs.1 Nr.3）。日本の『標準処理期間』は目安にすぎず
     沈黙は不許可でも許可でもない —— **別種の法的対象**なので同じ枠に入れない。

  ## `kyoninka.schema` を require していない理由

  既存 2 本（sanpai / kobutsu）は `:procedure/fee` と
  `:procedure/standard-period-days` に `schema/unverified` を付けている。
  この手続きは**その 2 つをどちらも収録していない**ので、`schema/unverified` を
  呼ぶ箇所が無い。

  - **手数料**: 連邦法は金額を定めない。州の Gebührenordnung が定め、しかも
    定額でなく**幅**で示される（実測: Niedersachsen は AllGO Kostentarif 2.1.35 で
    初回交付 平均 ca. 360 €、Berlin は 250–5,000 €、Hamburg は 50–1,000 €
    ないし UmwGebO の最低 371 €）。`:procedure/fee` の `:amount` はスカラ 1 個
    しか持てないので、どれを書いても残り 15 州について嘘になる。
    → **省いた。** 観測した 3 州の値は `:procedure/legal-questions` の
    `:land-gebuehren-und-praxis` に、どの州の話かを明記して保持する。
  - **標準処理期間**: ドイツにこの概念は無い。あるのは上記 3 か月の
    Genehmigungsfiktion で、これは『たいていこれくらいで返ってくる』ではなく
    『これを過ぎたら許可されたことになる』という別の法的効果。しかも法文の単位は
    暦月（『drei Monate』）であって日ではなく、日数への換算値はどの出典にも無い。
    → **省いた。** 3 か月という事実は `:submit` / `:respond-to-review` step の
    `:step/detail` に、起算点と非 EU/EEA 申請者への不適用（AbfAEV §10 Abs.8）
    ごと保持する。

  収録した値はすべて 2026-08-07 に一次資料（gesetze-im-internet.de の
  KrWG / AbfAEV / AVV / VwVfG、および州所管庁の公式ページ・Merkblatt）で確認した。
  裏の取れなかった値は書いていない。"
  )

(def procedure
  {:procedure/id :deu-abfall-transport
   :procedure/name "廃棄物の収集・運搬・取引・仲介の開業手続（ドイツ連邦・KrWG §53 届出／§54 許可の二段）"
   :procedure/law "Kreislaufwirtschaftsgesetz (KrWG) 第53条第1項（Anzeige）／第54条第1項（gefährliche Abfälle の Erlaubnis）。手続の細則は Anzeige- und Erlaubnisverordnung (AbfAEV)、危険性の判定は Abfallverzeichnis-Verordnung (AVV)（KrWG 第3条第5項 → 第48条）"
   ;; **法域は連邦。** KrWG / AbfAEV / AVV はいずれも連邦法で、州法ではない。
   ;; 州が持つのは所管（どの官庁が処理するか）と手数料。混ぜない。
   :procedure/jurisdiction "DEU"
   ;; **範囲は連邦全域。GBR の 2 本とは理由が逆。** England / England and Wales は
   ;; 法自体が devolve していて範囲が国より狭いのに対し、ドイツは範囲が国と一致する
   ;; —— KrWG §54 Abs.1 S.4「Die Erlaubnis nach Satz 1 gilt für die Bundesrepublik
   ;; Deutschland.」で明文化されている。
   ;;
   ;; 省略しても schema 上は「法域全域」を意味するが、**省略は『確認した』と
   ;; 『考えていない』を区別できない**ので明示する。州が決めるのは所管官庁と
   ;; 手数料だけで、適用範囲ではない —— そこに州名を書くと『ドイツの一部にしか
   ;; 効かない法律』という別の主張になる。州差は下の :land-* が持つ。
   :procedure/extent "Deutschland (Bund)"
   :procedure/source-urls
   [
                        "https://service.berlin.de/dienstleistung/326690/"
                        "https://www.gesetze-im-internet.de/abfaev/__10.html"
                        "https://www.gesetze-im-internet.de/abfaev/__12.html"
                        "https://www.gesetze-im-internet.de/abfaev/__13.html"
                        "https://www.gesetze-im-internet.de/abfaev/__3.html"
                        "https://www.gesetze-im-internet.de/abfaev/__4.html"
                        "https://www.gesetze-im-internet.de/abfaev/__5.html"
                        "https://www.gesetze-im-internet.de/abfaev/__6.html"
                        "https://www.gesetze-im-internet.de/abfaev/__7.html"
                        "https://www.gesetze-im-internet.de/abfaev/__9.html"
                        "https://www.gesetze-im-internet.de/avv/"
                        "https://www.gesetze-im-internet.de/avv/ （xml.zip 内 BJNR337910001.xml の Anlage）"
                        "https://www.gesetze-im-internet.de/krwg/__3.html"
                        "https://www.gesetze-im-internet.de/krwg/__53.html"
                        "https://www.gesetze-im-internet.de/krwg/__54.html"
                        "https://www.gesetze-im-internet.de/krwg/__55.html"
                        "https://www.gesetze-im-internet.de/vwvfg/__42a.html"
                        "https://www.gewerbeaufsicht.niedersachsen.de/download/89281/Merkblatt_Erlaubnis_Stand_12_2022.pdf"
                        "https://www.gewerbeaufsicht.niedersachsen.de/startseite/umweltschutz/kreislauf_und_abfallwirtschaft/anzeige_nach_53_krwg/anzeige-nach-53-krwg-106132.html"
                        "https://www.hamburg.de/politik-und-verwaltung/behoerden/bukea/themen/abfall-entsorgung/sammler-befoerderer-haendler-makler/abfallbefoerderung-erlaubnis-krwg-159504"
                        "https://www.hamburg.de/service/info/11277423/"
   ]
   :procedure/authority "申請者が Hauptsitz を置く州（Land）の所管当局（KrWG §53 Abs.1 S.3 / §54 Abs.1 S.3）。連邦官庁ではない。国内に本店が無い場合は、最初に収集・運搬・取引・仲介を行う州の当局（AbfAEV §7 Abs.2 / §9 Abs.2）"
   ;; **ここが連邦と州の境目。** 法は連邦、窓口と手数料は州。
   ;; 州の官庁名は実在確認できた 3 州だけ挙げ、「州ごとに違う」を明示して残り 13 州を
   ;; 推測で埋めない。
   :procedure/window "州ごとの所管庁（実在確認済みの例: Niedersachsen = Staatliches Gewerbeaufsichtsamt Hildesheim / Hamburg = Behörde für Umwelt, Klima, Energie und Agrarwirtschaft / Berlin = Senatsverwaltung für Mobilität, Verkehr, Klimaschutz und Umwelt。他 13 州は未確認 — 自社の Hauptsitz の州を個別に確認すること）。電子申請は州共通の eAEV ポータル https://eaev.gadsys.de（AbfAEV §8 / §11 が命じる『bundesweit einheitliches informationstechnisches System』）。書面は AbfAEV Anlage 2（届出）／Anlage 3（許可申請）の様式"
   ;; **法定の有効期間は無い。** §54 Abs.1 S.4 の許可は連邦全域に効き、州所管庁の
   ;; 公式 Merkblatt（Nds. Gewerbeaufsichtsamt Hildesheim, Stand 05/2026）も
   ;; 「generell bundesweit …, für alle Abfallarten und zeitlich unbefristet」と明記。
   ;; ただし当局は §54 Abs.2 の Nebenbestimmung で、届出側は §53 Abs.3 で
   ;; 明文上「zeitlich befristen」できる。nil は「更新制度が無い」であって
   ;; 「絶対に期限が付かない」ではない。
   :procedure/valid-years nil

   ;; ITAD（PC 廃棄）で扱う廃棄物と、それが §53 と §54 のどちらに落ちるか。
   ;; **AVV の Abfallschlüssel に * が付いていれば gefährlich**（AVV §3 Abs.1）。
   ;; 以下の 6 コードは AVV 本文（gesetze-im-internet.de の XML）で実際に読んで確認した。
   :procedure/waste-categories
   [{:id :geraete-mit-gefaehrlichen-bauteilen :name "16 02 13* gefährliche Bauteile enthaltende gebrauchte Geräte"
     :itad-note "事業所由来の PC・サーバでバッテリー等の危険部品を含むもの。* 付き = gefährlich → §54 の Erlaubnis 側"}
    {:id :geraete-sonstige :name "16 02 14 gebrauchte Geräte（16 02 09〜13 に当たらないもの）"
     :itad-note "危険部品を外した／含まない機器。* 無し = nicht gefährlich → §53 の Anzeige 側"}
    {:id :entfernte-bauteile :name "16 02 16 aus gebrauchten Geräten entfernte Bauteile（16 02 15* 以外）"
     :itad-note "抜去した HDD・メモリ・基板。抜いた部品が 16 02 15*（gefährliche Bauteile）に当たるかで分岐する"}
    {:id :weee-haushalt-gefaehrlich :name "20 01 35* gebrauchte elektrische und elektronische Geräte, die gefährliche Bauteile enthalten"
     :itad-note "家庭由来（宅配回収）の機器で危険部品を含むもの。宅配回収 ITAD はここに落ちやすい"}
    {:id :weee-haushalt-sonstige :name "20 01 36 gebrauchte elektrische und elektronische Geräte（20 01 21/23/35 以外）"
     :itad-note "家庭由来で危険部品を含まないもの"}
    {:id :batterien-sonstige :name "16 06 05 andere Batterien und Akkumulatoren"
     :itad-note "PC から抜いたバッテリー。鉛蓄電池（16 06 01*）等に当たらないもの。当たれば gefährlich"}]

   :procedure/requirements
   [{:requirement/id :abfallklassifizierung
     :requirement/name "廃棄物の危険性分類（gefährlich かどうか）の確定"
     :requirement/detail "KrWG 第3条第5項は『第48条の法規命令で定められた廃棄物が gefährlich、それ以外は nicht gefährlich』とし、その命令が AVV。AVV 第3条第1項により Abfallschlüssel に * が付いた種類が gefährlich（同条第2項で EU 廃棄物枠組指令 2008/98/EC 附属書III の危険特性 HP1-HP15 に接続する）。**この分類が §53 の届出で足りるか §54 の許可が要るかを決める**ので、手続きの最初に確定させる。"
     :requirement/blocking? true}
    {:requirement/id :zuverlaessigkeit
     :requirement/name "Zuverlässigkeit（信頼性）"
     :requirement/detail "KrWG §53 Abs.2 S.1 / §54 Abs.1 S.2 Nr.1。事業主および事業の指揮・監督に責任を持つ者が対象。AbfAEV §3 Abs.2 は『通常は信頼性が無い』場合を具体化する — 環境刑法・廃棄物法・水法・化学品法・遺伝子技術法・原子力法、食品/医薬品/植物保護/感染症法、営業/労働保護/運送/危険物法、麻薬/武器/爆発物法の違反により、届出または許可申請前の5年以内に **2,500 ユーロ超の過料**を科されたか刑を受けた場合、および反復的・重大な義務違反があった場合。"
     :requirement/blocking? true}
    {:requirement/id :fachkunde
     :requirement/name "Fachkunde（専門知識、経営責任者）"
     :requirement/detail "許可側（AbfAEV §5 Abs.1）は ①申請する活動についての**2年の実務**（当該分野の大学/専門大学卒、商業・技術系の専門学校/職業訓練修了、または Meister 資格があれば1年に短縮）に加え、②**所管庁が承認した Lehrgang**（AbfAEV Anlage 1 の内容）の受講が必須。届出側（AbfAEV §4）は原則②が不要で実務経験のみだが、当局が §4 Abs.5 で受講と定期的継続教育を命じうる。Lehrgang は**どの州で受けてもよい**（提供者が承認を持っていればよい。Nds. Merkblatt 明記）。"
     :requirement/blocking? true}
    {:requirement/id :fortbildung
     :requirement/name "Fortbildung（継続教育、少なくとも3年ごと）"
     :requirement/detail "AbfAEV §5 Abs.3。許可対象の事業主・指揮監督責任者は『regelmäßig, mindestens alle drei Jahre』承認 Lehrgang に参加し、**求められずとも当局に証明する**義務を負う。許可取得後に発生する継続義務であって、初回申請の要件ではない。"
     :requirement/blocking? false}
    {:requirement/id :sachkunde-personal
     :requirement/name "Sachkunde（その他の職員）"
     :requirement/detail "AbfAEV §6。その他の職員は Einarbeitungsplan（習熟計画）に基づき社内で習熟させ、必要な最新知識を保持させる。継続教育の必要性は事業主または指揮監督責任者が判定する。当局は公益上必要なら計画の書面化と提出を命じうる。KrWG §53 Abs.3 S.3 は、この Sachkunde が証明されない場合に**届出済みの活動を禁止しなければならない**と定めるので、書類要件ではないが活動の存続を左右する。"
     :requirement/blocking? true}
    {:requirement/id :kfz-haftpflicht
     :requirement/name "Kfz-Haftpflichtversicherung（危険廃棄物を公道で運ぶ場合）"
     :requirement/detail "AbfAEV §9 Abs.3 S.1 Nr.8。危険廃棄物を公道で運ぶ Sammler / Beförderer は自動車損害賠償責任保険の証明を申請に添付する。同 Nr.7 の Betriebshaftpflicht / Umwelthaftpflicht は『sofern solche Versicherungen vorhanden sind』（存在する場合）にとどまり、連邦法は最低補償額を定めない — 最低額を求めるのは州の運用（:land-gebuehren-und-praxis を参照）。"
     :requirement/blocking? true}]

   ;; AbfAEV §9 Abs.3 S.1 の 8 項目（許可申請）を条文の順で写したもの。
   ;; 届出（§53）側は AbfAEV §7 Abs.1 により Anlage 2 の様式が本体で、
   ;; EfB / EMAS の免除に依る場合だけ証明書を添える。
   :procedure/documents
   [{:document/id :antrag-vordruck :document/name "申請様式（許可は AbfAEV Anlage 3、届出は Anlage 2。電子申請では Anlage 2 の『Unterschrift』欄は無くなる — AbfAEV §8 Abs.1 Nr.1）" :document/who :company}
    {:document/id :gewerbeanmeldung :document/name "Gewerbeanmeldung（営業開始届の写し。AbfAEV §9 Abs.3 Nr.1）" :document/who :company}
    {:document/id :registerauszug :document/name "Handels-, Vereins- oder Genossenschaftsregister の抄本（登記がある場合。同 Nr.2）" :document/who :company}
    {:document/id :gzr-firma :document/name "Gewerbezentralregister の firmenbezogene Auskunft, Belegart 9（法人・人的団体の場合。同 Nr.3）" :document/who :company}
    {:document/id :gzr-person :document/name "Gewerbezentralregister の personenbezogene Auskunft, Belegart 9（事業主および指揮監督責任者。同 Nr.4、GewO §150 Abs.5）" :document/who :officers}
    {:document/id :fuehrungszeugnis :document/name "Führungszeugnis, Belegart OG（事業主および指揮監督責任者。同 Nr.5、BZRG §§30 Abs.5・32 Abs.4。住所地自治体で申請）" :document/who :officers}
    {:document/id :fachkunde-nachweis :document/name "Fachkunde の証明（実務経験＋承認 Lehrgang の修了証。同 Nr.6）" :document/who :officers}
    {:document/id :haftpflicht :document/name "Betriebshaftpflichtversicherung および活動に対応した Umwelthaftpflichtversicherung の証明（**存在する場合**。同 Nr.7）" :document/who :company}
    {:document/id :kfz-haftpflicht :document/name "Kfz-Haftpflichtversicherung の証明（危険廃棄物を公道で運ぶ Sammler / Beförderer。同 Nr.8）" :document/who :company}
    {:document/id :efb-zertifikat :document/name "Entsorgungsfachbetrieb の有効な証明書（KrWG §56 Abs.3。§54 Abs.3 Nr.2 の許可免除に依り届出で済ませる場合に添付。AbfAEV §7 Abs.1 S.2）" :document/who :company}
    {:document/id :emas-urkunde :document/name "EMAS 登録証（AbfAEV §12 Abs.1 Nr.4 の許可免除に依る場合。後続の登録証は求められずとも提出する。AbfAEV §7 Abs.1 S.3-4）" :document/who :company}]

   :procedure/steps
   [{:step/id :classify-waste :step/order 1 :step/requires-human false
     :step/title "扱う廃棄物を AVV で分類し、§53（届出）か §54（許可）かを確定する"
     :step/detail "AVV の Abfallschlüssel に * が付けば gefährlich（AVV §3 Abs.1）。ITAD なら 16 02 13* / 16 02 14 / 16 02 16 / 20 01 35* / 20 01 36 / 16 06 05 の分岐を先に決める。**この 1 手で以降の様式・書類・費用・所要期間がすべて変わる**ので、他の step より前に置く。"}
    {:step/id :verify-current-rules :step/order 2 :step/requires-human true
     :step/title "自社 Hauptsitz の州の所管庁で、最新の様式・手数料・提出方法を確認"
     :step/detail "**手数料はこの手続き data に収録していない** — 連邦法は金額を定めず、州の Gebührenordnung が幅で定めるため。所管庁と、その州の Gebührenordnung 上の項目・金額をここで確定させる。電子申請（eAEV）に電子署名カードが要るかも州の運用で異なる。"
     :step/errand {:kind :verify-authority-info :draft-via :tayori
                   :evidence-schema {:authority :string :verified :map
                                     :source :string :date :iso-date}}}
    {:step/id :check-exemptions :step/order 3 :step/requires-human false
     :step/title "免除規定に当たらないかを確認する（当たれば手続き自体が消えるか軽くなる）"
     :step/detail "届出の免除: AbfAEV §7 Abs.8（法規命令に基づく製造者・販売者の非危険廃棄物の引取り）、同 Abs.9（『gewöhnlich und regelmäßig』でない収集・運搬。暦年で**非危険 20 トン／危険 2 トン**を超えれば常態と推定される）。許可の免除: KrWG §54 Abs.3（公法上の処理主体、および当該活動について認証された Entsorgungsfachbetrieb）、AbfAEV §12 Abs.1 の 6 類型（①本業が廃棄物でない事業者、②製造者・販売者の引取り、③使用済自動車の引渡し、④EMAS 登録事業所の該当業種、⑤海上輸送、⑥**Paket-, Express- und Kurierdienste**）。**許可を免れても §53 の届出義務は残る。** 当局は §12 Abs.2 で免除にかかわらず許可手続を命じうる。"}
    {:step/id :resolve-legal-questions :step/order 4 :step/requires-human true
     :step/title "事業スキームの法的整理（ドイツの Umweltrecht 専門の弁護士に諮る）"
     :step/detail ":procedure/legal-questions を諮る。特に『自社が Händler / Makler に当たるか』と『宅配回収が AbfAEV §12 Abs.1 Nr.6 の KEP 免除に乗るか』は、当たり外れで手続きの有無そのものが変わる。"
     :step/errand {:kind :consult-professional :draft-via :tayori
                   :evidence-schema {:office :string :date :iso-date
                                     :resolutions :map}}}
    {:step/id :acquire-fachkunde :step/order 5 :step/requires-human true
     :step/title "承認 Lehrgang（AbfAEV Anlage 1 の内容）の受講予約と受講"
     :step/detail "許可側は実務経験に加えて必須（AbfAEV §5 Abs.1 Nr.2）。提供者が所管庁の承認を持っていれば**どの州で受けてもよい**ので、自州で日程が無ければ他州を探す。承認の有無は提供者に都度確認する。"
     :step/errand {:kind :book-course :draft-via :koyomi
                   :evidence-schema {:provider :keyword :course :string :date :iso-date
                                     :attendee :string :confirmation-no :string}}}
    {:step/id :collect-documents :step/order 6 :step/requires-human false
     :step/title "申請書類一式の収集（:procedure/documents のチェックリスト消化）"
     :step/detail "Führungszeugnis と Gewerbezentralregister の抄本は**取得から3か月以内・原本**で提出するのが州の運用（Nds. Merkblatt）。取り寄せに時間がかかる一方で古くなると使えないので、他の書類より後ろに置く。AbfAEV §9 Abs.4 により Nr.1・2・6・7・8 は写しでよいが、真正性に疑いがあれば当局は原本を求めうる。"
     :step/errand {:kind :collect-documents :draft-via nil
                   :evidence-schema {:doc :keyword :obtained-date :iso-date}}}
    {:step/id :submit :step/order 7 :step/requires-human true
     :step/title "届出（Anlage 2）または許可申請（Anlage 3）の提出と手数料納付"
     :step/detail "提出は human gate。**許可側はここから 3 か月の Genehmigungsfiktion が動く** — 当局が完全な申請の受領確認（AbfAEV §10 Abs.1、開始日と満了日が明示される）を出したのち、当局が動かないまま 3 か月経つと許可されたものとみなされる（KrWG §54 Abs.6 S.2 i.V.m. VwVfG §42a。§42a Abs.2 の既定期間が 3 か月で、AbfAEV は別段の期間を定めていない）。ただし **この擬制は申請者が EU/EEA の国籍・法人であることが条件**で、それ以外には受領確認・期間告知の規定が適用されない（AbfAEV §10 Abs.8）。届出側にはそもそも擬制が無く、当局は受領を『unverzüglich』書面で確認するだけ（KrWG §53 Abs.1 S.2、AbfAEV §7 Abs.5 は記入済み様式の返送をもって確認とする）。控えと Vorgangsnummer / Kennnummer を台帳に記録する。"}
    {:step/id :respond-to-review :step/order 8 :step/requires-human true
     :step/title "審査対応（不備の補正）"
     :step/detail "申請が不完全なら当局は遅滞なく追完すべき書類を通知し、**3 か月の期間は完全な書類の到達をもって初めて起算される**（AbfAEV §10 Abs.2）。届出側も不完全なら追完を求められる（AbfAEV §7 Abs.4）。"}
    {:step/id :post-grant :step/order 9 :step/requires-human true
     :step/title "取得後の継続義務"
     :step/detail "①**携行義務**（AbfAEV §13）: 届出なら当局が確認した届出書の写し／印刷物、許可なら許可の写し（**非危険廃棄物を運ぶときも携行する** — §13 Abs.2 S.2）、擬制で許可を得た場合は申請書と受領確認の写し。鉄道車両による収集・運搬では免除。②**車両標識**（KrWG §55 Abs.1）: 公道で運ぶ車両には出発前に反射白色の警告板（A-Schild）2 枚。ただし『im Rahmen wirtschaftlicher Unternehmen』の Sammler / Beförderer には適用されない（同 S.2）。要件は AbfVerbrG §10 に従う。③**継続教育**（AbfAEV §5 Abs.3）: 少なくとも 3 年ごとに承認 Lehrgang を受け、求められずとも当局に証明。④**変更時**: 届出は重要事項が変われば再度の届出（AbfAEV §7 Abs.7）、許可は基礎事情が変われば新しい許可が必要で、指揮監督責任者の交替は届出で足りる（AbfAEV §10 Abs.6）。"}]

   ;; 結論を出さずに保持する法的論点。
   :procedure/legal-questions
   [{:question/id :gefaehrlichkeit
     :question/title "ITAD の PC・電子機器が gefährliche Abfälle に当たるか"
     :question/detail "AVV は同じ『使用済み機器』を、危険部品を含むか否かで 16 02 13*／16 02 14（事業所由来）、20 01 35*／20 01 36（家庭由来）に割る。バッテリー・特定の基板・ディスプレイの扱い次第で * 側に落ち、その瞬間に §53 の届出が §54 の許可に変わる。回収時点で分類が確定しない混合ロットをどう扱うかも含めて詰める。"
     :question/status :open}
    {:question/id :haendler-makler-gaitousei
     :question/title "自ら運ばない ops 層が Händler / Makler に当たるか"
     :question/detail "KrWG §53 Abs.1 / §54 Abs.1 の名宛人は Sammler・Beförderer に加えて **Händler（取引）と Makler（仲介）**。許可業者に取り次ぐだけのプラットフォームがこれに当たれば、自社も届出（危険廃棄物なら許可）の対象になる。日本の『運送委託で収集運搬許可を回避する』設計がここでは通らない可能性が高い。Makler の外延を示す判例はワークスペースに未収載（cloud-itonami-licensed-operator の :known-gaps でも同じ穴が挙がっている）。"
     :question/status :open}
    {:question/id :kep-ausnahme
     :question/title "宅配回収が AbfAEV §12 Abs.1 Nr.6 の KEP 免除に乗るか"
     :question/detail "AbfAEV §12 Abs.1 Nr.6 は『Paket-, Express- und Kurierdienste の枠内で収集・運搬する Sammler / Beförderer』を、その運送約款が危険物輸送の安全法令を考慮している限り、危険廃棄物の**許可義務から**除外する。宅配回収 ITAD がここに乗れば §54 が外れるが、**§53 の届出義務は残る**。また当局は §12 Abs.2 で公益上必要なら許可手続を命じうる。誰が『KEP の枠内』の主体なのか（運送会社か、荷主である自社か）を確定させる必要がある。"
     :question/status :open}
    {:question/id :wirtschaftliche-unternehmen
     :question/title "自社が『im Rahmen wirtschaftlicher Unternehmen』に当たるか"
     :question/detail "廃棄物の取扱いが本業でない事業者は AbfAEV §12 Abs.1 Nr.1 で危険廃棄物の**許可義務から**外れ、Fachkunde も §4 Abs.4（本業に必要な職業資格で足りる）という軽い規律になり、KrWG §55 Abs.1 S.2 で A-Schild も不要になる。他方 AbfAEV §7 Abs.9 の 20 トン／2 トン閾値もこの類型に効く。**ITAD を事業として掲げている以上ここには当たらない可能性が高い**が、当たる/当たらないで規律の重さが大きく変わるので明示的に判定する。"
     :question/status :open}
    {:question/id :schwellenwert
     :question/title "年間取扱量が届出免除の閾値に収まるか"
     :question/detail "AbfAEV §7 Abs.9: 『gewöhnlich und regelmäßig』でない収集・運搬は届出義務を免れるが、暦年の合計が**非危険 20 トンまたは危険 2 トン**を超えれば常態と推定される。PC は 1 台 10kg 前後なので、非危険側 20 トンは概ね年 2,000 台規模。事業計画上の想定台数がこの線のどちら側かを最初に見積もる。"
     :question/status :open}
    {:question/id :land-gebuehren-und-praxis
     :question/title "手数料と保険の最低額は州ごとに違う — 自社はどの州の運用に服するか"
     :question/detail "連邦法は金額を定めない。2026-08-07 に確認した公式値: **Niedersachsen** は AllGO の Kostentarif 2.1.35 により初回交付が『durchschnittlich ca. 360 €』（平均額であって定額ではない）、**Berlin** は運搬許可の交付が 250–5,000 €、重要変更の決定が 50–5,000 €、期限付き許可が 50–5,000 €、**Hamburg** は『je nach Aufwand 50,00 EUR - 1.000,00 EUR』（サービス案内）と『最低 371 €、事務量に応じて増額、UmwGebO による』（所管庁の案内）で公式ページ間に食い違いがある。保険も同様で、AbfAEV §9 Abs.3 Nr.7 は連邦法上『存在する場合』の添付にとどまるのに、Niedersachsen は Sammler / Beförderer に **Umwelt-Haftpflichtversicherung 一律最低 100 万ユーロ**の証明を求めている。**残り 13 州は未確認。** 手数料も処理実務も、この data ではなく自社 Hauptsitz の州の Gebührenordnung が正本。"
     :question/status :open}
    {:question/id :andere-genehmigungen
     :question/title "KrWG の許可だけでは足りない — 併存する他法の許可"
     :question/detail "所管庁の Merkblatt が明記する: 『Die Erlaubnis ist eine ausschließlich nach dem KrWG ergehende Entscheidung. Andere Genehmigungen, Erlaubnisse, Konzessionen usw. (insbesondere nach dem Güterkraftverkehrsgesetz und den Gefahrgutverordnungen) müssen unabhängig von der abfallrechtlichen Erlaubnis vorliegen.』。貨物自動車運送事業（GüKG）と危険物輸送（ADR / GGVSEB 系）の規律は別建てで、KrWG §55 Abs.3 も危険物輸送法令は影響を受けないと定める。この data はそれらを収録していない。"
     :question/status :open}
    {:question/id :efb-zertifizierung
     :question/title "Entsorgungsfachbetrieb 認証を取って許可を免れる経路の是非"
     :question/detail "KrWG §54 Abs.3 Nr.2 により、当該許可対象活動について認証された Entsorgungsfachbetrieb（KrWG §56）は許可義務から外れる（届出には §56 Abs.3 の有効な証明書を添付し、車載も要る — AbfAEV §7 Abs.1 S.2 / §13 Abs.1 S.4）。許可申請と EfB 認証取得のどちらが速く安いか、また EfB 認証が他の商流（排出事業者からの受注要件）でも効くかを併せて判断する。KrWG §56 の認証要件そのものはこの data に未収載。"
     :question/status :open}]})
