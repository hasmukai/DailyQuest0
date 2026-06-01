# DailyQuest 🎮

DailyQuestは、日々のルーティンやタスクをゲーム感覚でこなし、報酬を得るための習慣化支援・タスク管理アプリです。
自分で設定したタスク（Quest）をクリアすることで「偉すぎポイント（EP）」を獲得し、それを好きな報酬（お金、趣味の時間など）に変換して消費・記録することができます。

## ✨ 主な機能

*   **🎯 3種類のクエスト管理**
    *   **Daily**: 毎日繰り返すルーティン
    *   **Weekly**: 1週間のうちに達成したい目標
    *   **Temporary**: 単発のタスク
*   **💰 ウォレット＆ご褒美システム**
    *   獲得したEPを独自のレート（例: 100EP = 10円、500EP = 1時間のゲーム）で変換可能。
    *   自分だけのご褒美を設定し、消費履歴を記録できます。
*   **📊 統計とストリーク（連続達成記録）**
    *   GitHubライクな「芝生」グラフ（ヒートマップ）で、日々の努力を視覚化。
    *   現在のストリークや最大ストリークを確認し、モチベーションを維持できます。
*   **🔔 常駐通知機能（進捗バー）**
    *   アプリを開かなくても、スマートフォンの通知領域からデイリー・ウィークリークエストの達成状況をプログレスバーでリアルタイムに確認できます。

## 🛠️ 技術スタック

本アプリは最新のAndroid推奨アーキテクチャ（MVVM）とモダンなUIツールキットを用いて構築されています。

*   **開発言語**: Kotlin
*   **UIフレームワーク**: Jetpack Compose (Material Design 3)
*   **アーキテクチャ**: MVVM (Model-View-ViewModel) + Repository
*   **ローカルデータベース**: Room Database
*   **ルーティング**: Jetpack Navigation Compose
*   **非同期処理**: Kotlin Coroutines & Flow
*   **バックグラウンド処理**: Foreground Service (通知用)

## 📁 プロジェクト構造

```text
DailyQuest0/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/example/dailyquest0/
│   │   ├── MainActivity.kt        # エントリーポイント
│   │   ├── DailyQuestApplication.kt
│   │   ├── data/                  # データベース (Room Entities, DAO, Repository)
│   │   ├── ui/                    # UI (Jetpack Compose 画面群, ViewModel, Theme)
│   │   ├── service/               # バックグラウンドサービス (常駐通知など)
│   │   └── utils/                 # 日付処理などのユーティリティ
│   └── res/                       # リソース (アイコン、独自通知レイアウトなど)
└── build.gradle.kts
```

## 🚀 ビルドと実行方法

1. Android Studioを開き、本プロジェクトをインポートします。
2. ターゲットのAPIレベル（SDK 36など）がインストールされていることを確認します。
3. エミュレーターまたは実機を接続し、「Run」ボタンを押してビルド・実行します。
4. ※ 初回起動時は「通知の送信」の権限を許可してください（常駐通知を利用するため）。
