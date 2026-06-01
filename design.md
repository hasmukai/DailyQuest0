# DailyQuest 設計・仕様書 (Detailed Design Document)

## 1. アプリのコンセプトと目的
DailyQuestは、日々のルーティンや目標（タスク）をゲーム感覚でこなし、報酬を得るための習慣化支援アプリです。
自分で設定したタスク（Quest）をクリアすることで「偉すぎポイント（EP: Erasugi Points）」を獲得し、それをあらかじめ設定した自分へのご褒美（お金、趣味の時間、お菓子など）に変換して消費・記録することで、モチベーションを維持します。

## 2. アプリの基本仕様

### 2.1. ロジカルな日付変更線 (Logical Date)
本アプリでは、生活リズムに合わせるため**日付の切り替わりを午前4:00**としています。
深夜0:00〜3:59までのタスク完了は「前日分のタスク」として扱われます。
同様に、ウィークリークエストの起点も「月曜日の午前4:00」を週の始まりとして計算されます。

### 2.2. EP (偉すぎポイント) システム
- クエストを完了すると、そのクエストに設定されたEPが `UserStats`（現在の保有EPと累計EP）に即座に加算されます。
- クエストの完了をキャンセルすると、獲得したEPは減算（没収）されます。
- EPはWallet機能を通じて、あらかじめ定義されたレートで別の価値（円など）に変換・消費することができます。

## 3. コア機能と画面構成 (UI/UX)

アプリは大きく3つのメイン画面（タブ）で構成されています。

### 3.1. Home Screen (クエスト管理)
- **タブ切り替え**: `ALL`, `DAILY`, `WEEKLY`, `TEMPORARY` の4つのフィルタでクエスト一覧を切り替え表示。
- **クエスト一覧**: タップで完了/未完了をトグル。完了時はUI上で打消し線と透過処理が入り、即座にEPが加算されます。
- **追加・編集・削除**: FAB（Floating Action Button）から新規クエストを追加。リストの長押しで編集・削除メニューを表示。
- **クエストの3分類**:
  - `DAILY`: 毎日リセットされるルーティン。その日（〜翌3:59）に完了したかが記録されます。
  - `WEEKLY`: その週（月曜4:00〜翌月曜3:59）の間に1度でも達成すれば完了となる目標。
  - `TEMPORARY`: 一度完了すると、チェック状態が維持される（または手動で削除する）単発タスク。

### 3.2. Wallet Screen (ご褒美・口座管理)
- **Walletの管理**: EPの変換先となる口座（例: 「現金口座」「ゲーム時間」など）を複数作成可能。名前、単位（円、分など）、アイコンを設定。
- **EPの変換 (Exchange)**: 各Walletに対して、独自の変換レート（例: 「100EP = 10円」「500EP = 30分」）を設定し、EPを消費して残高をチャージ。
- **残高の消費**: チャージした残高を、任意のメモ（例: 「コンビニでアイスを買った」）とともに消費し、取引履歴（Transaction）として記録。
- **履歴表示**: Walletごとにチャージ履歴と消費履歴を一覧表示。

### 3.3. Stats Screen (統計・記録)
- **芝生グラフ (Heatmap)**: 過去80日間のデイリークエスト完了状況を、GitHubのコントリビューショングラフ風にヒートマップ（色の濃淡）で可視化。
- **ストリーク機能**:
  - `Current Streak`: 何日連続でクエストをこなしているか（昨日または今日クエストをこなしていれば継続中と判定）。
  - `Max Streak`: 過去最大の連続記録。
- **累計記録**: これまでに完了したクエストの総数を表示。
- **EP履歴グラフ**: 日々の保有EPの推移を折れ線グラフで可視化するためのスナップショットデータを利用。

### 3.4. 常駐通知機能 (Foreground Service)
- **QuestProgressService**: アプリを開かなくても、スマートフォンの通知領域でクエストの進捗を確認できる常駐通知。
- Android 14+の要件に適合する `specialUse` の Foreground Service として稼働。
- 「Daily: X/Y」「Weekly: A/B」といったプログレスバーを独自レイアウト(`RemoteViews`)を用いて、折りたたまれた状態でも常に表示し、Room DBの変更に即座に反応してゲージが更新されます。通知タップで直接HomeScreenへ遷移します。

## 4. データベース設計 (Room Database)

### Entities (テーブル設計)
1. **`UserStats`**
   - ユーザーの全体的なステータス。レコードは常にID=1の1行のみ存在。
   - `totalEp` (累計獲得EP), `currentEp` (現在使用可能なEP)
2. **`DailyEpSnapshot`**
   - 日々のEP残高の推移を記録するためのスナップショット。
   - `id`, `date` (YYYY-MM-DD), `balance` (その日の最終的なEP残高)
3. **`Quest`**
   - 設定されたタスクのマスターデータ。
   - `id`, `title` (タスク名), `epReward` (獲得EP), `type` (DAILY/WEEKLY/TEMPORARY), `iconName` (マテリアルアイコン名)
4. **`DailyQuestLog`**
   - どのクエストが、いつ完了されたかの履歴トランザクション。
   - `id`, `questId` (紐づくQuest ID), `date` (完了日 YYYY-MM-DD), `isCompleted` (完了状態)
5. **`Wallet`**
   - ご褒美を入れる器（口座）。
   - `id`, `name` (口座名), `unit` (単位), `iconName`
6. **`ExchangeRate`**
   - WalletごとのEP変換レート設定。
   - `id`, `walletId`, `requiredEp` (必要なEP), `rewardedAmount` (得られる残高)
7. **`WalletTransaction`**
   - Wallet残高の増減履歴。
   - `id`, `walletId`, `amount` (増減額: 正ならチャージ、負なら消費), `memo` (理由/使い道), `createdAt` (日時タイムスタンプ)

## 5. アプリケーション・アーキテクチャ

- **言語**: Kotlin
- **UI層**: Jetpack Compose (Material Design 3)。`ViewModel` を通じて `StateFlow` を監視し、リアクティブにUIを描画。
- **ドメイン/データ層**: `AppRepository` が `AppDao` (Room) を抽象化し、Flowを組み合わせてViewModelにデータを提供。
- **Applicationクラス**: `DailyQuestApplication` を用いて、`AppDatabase` と `AppRepository` をシングルトンとして保持し、UI (Activity) と Service (Foreground Service) の双方から安全かつ一貫してアクセスできる構造。
- **依存関係管理**: Gradle (Kotlin DSL / kts) および Version Catalogs を採用。
