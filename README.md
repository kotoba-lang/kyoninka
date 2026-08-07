# kyoninka（許認可）

The closed eight-status transition and explicit human-approval gate also has a
native `.kotoba` kernel. It compiles to restricted JavaScript and Wasm without
a JVM runtime. Procedure text, legal questions, current authority information,
fees, documents, persistence, and external actions remain host-owned CLJC data
and code.

許認可手続きを **procedure-as-data**（Datomic 互換 EDN + 純関数 + 状態機械 +
append-only 台帳イベント）で表すライブラリ。ADR-2607141620（com-junkawasaki/root）。

**2026-08-07 に日本以外へ広げた。** それまでは日本専用で、schema に法域属性が
1 つも無く「日本であること」が暗黙だった（`:procedure/fee-jpy` は通貨を属性名に
焼いていた）。いまは `:procedure/jurisdiction`（ISO 3166-1 alpha-3）が必須で、
手数料は通貨中立（最小単位の整数 + `:minor-unit`）。

| ns | 法域 | 手続き | 根拠法 | 窓口 |
|---|---|---|---|---|
| `kyoninka.sanpai` | JPN | 産業廃棄物収集運搬業許可（東京都・積替え保管なし） | 廃棄物処理法 14条1項 | 東京都環境局 |
| `kyoninka.kobutsu` | JPN | 古物商許可（営業所: 千代田区丸の内） | 古物営業法 3条 | 管轄警察署経由 東京都公安委員会 |
| `kyoninka.gbr-waste-carrier` | GBR（England のみ） | 廃棄物運搬業登録 upper tier | COPA(A) 1989 s.1 + SI 2011/988 Part 8 | Environment Agency（GOV.UK オンライン） |
| `kyoninka.gbr-scrap-metal` | GBR（England and Wales） | スクラップ金属業免許（site / collector） | Scrap Metal Dealers Act 2013 | 地方自治体（council） |
| `kyoninka.deu-abfall-transport` | DEU | 廃棄物収集運搬（§53 届出 / §54 許可） | KrWG §53/§54 + AbfAEV | 州（Land）当局 |

**収録できなかった値は書いていない。** 英国スクラップ金属と独の手数料は
`:amount` を持たない —— 額を決めるのが手続きの外（council / 州）で、実測すると
英国は £181〜£804.78（4 倍以上）、独は州ごとに幅で定められる。代表額を選べば
残り全部で嘘になる。標準処理期間も、法にも公表にも決定期限が無い制度が 3 本ある。
**空欄は手抜きではなく測定結果**で、`:verify` がその区別を持つ。

## 設計原則（正直さがコードの形をしている）

1. **標準値には必ず `:verify` フラグ** — 手数料・標準処理期間・様式は改定される。
   「最新の実値を窓口で確認する」ことが全手続きの `:step/order 1` にコードとして
   組み込まれている。収録値をそのまま信じて申請してはいけない。
2. **法的論点は `:legal-questions` として保持し、結論を出さない** — 例: 宅配回収
   ITAD の廃棄物該当性（買取 = 古物営業 vs 廃棄物 = 廃掃法、運送委託の扱い、
   許可の地域範囲）。行政書士確認で `:question/status :resolved` にするまで
   スキームを断定しない。
3. **提出・官庁接触・支払いは human gate** — 状態機械（`schema/transitions`）の
   `:human` 遷移と `:step/requires-human true` の step は `:human-approved true`
   なしに進められない。ライブラリは next-action を**提案するだけ**。
4. **台帳が正** — case map は `progress/replay` で台帳イベントから再構成できる射影。

## 使い方

```clojure
(require '[kyoninka.sanpai :as sanpai]
         '[kyoninka.progress :as p])

(def c (p/new-case sanpai/procedure {:case-id :itad-sanpai :applicant "Gftd Japan株式会社"}))
(p/next-actions sanpai/procedure c)   ; => 最初は必ず「最新の実値確認」「行政書士相談」
(p/checklist sanpai/procedure c)      ; => 書類チェックリスト
(p/summary sanpai/procedure c)        ; => 進行サマリ
(p/advance c {:to :preparing})        ; => {:ok? true :case ...}
```

消費者: `gftdcojp/cloud-itonami` の `cloud_itonami/license.cljc`
（Gftd Japan の 2 case を台帳 `resources/licenses/itad-license-ledger.edn` で進行管理）。

## テスト

```bash
nbb --classpath "src:test" test/run_tests.cljs   # 第一経路
clojure -X:test                                   # JVM (compat)
```
