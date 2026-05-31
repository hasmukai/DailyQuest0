# DailyQuest0

## プロジェクト概要
DailyQuest0 は、KotlinとJetpack Composeを使用して構築されたAndroidアプリケーションです。MVVM（Model-View-ViewModel）アーキテクチャを採用しており、ローカルデータベースにはRoomを使用しています。

## プロジェクト構造

プロジェクトの主なファイル・ディレクトリ構造は以下の通りです。

```text
DailyQuest0/
├── app/
│   ├── build.gradle.kts       # アプリケーションレベルのビルドスクリプト
│   └── src/
│       ├── androidTest/       # インストルメンテーションテスト（UIテストなど）
│       ├── test/              # ユニットテスト
│       └── main/
│           ├── AndroidManifest.xml # アプリケーションのマニフェストファイル
│           ├── res/           # リソースファイル（ドローアブル、文字列、テーマなど）
│           └── java/com/example/dailyquest0/
│               ├── MainActivity.kt    # アプリのエントリーポイントとなるメインアクティビティ
│               ├── data/              # データアクセス層
│               │   ├── AppDatabase.kt # Roomデータベースの構成
│               │   ├── dao/           # データアクセスオブジェクト (AppDao.ktなど)
│               │   ├── entity/        # データベースのエンティティ (Entities.ktなど)
│               │   └── repository/    # リポジトリパターンを実装するクラス群
│               └── ui/                # ユーザーインターフェース層 (Jetpack Compose)
│                   ├── navigation/    # 画面遷移のルーティング設定
│                   ├── screens/       # 各画面（スクリーン）のコンポーザブル関数
│                   ├── theme/         # アプリ全体のテーマ、カラー、タイポグラフィの設定
│                   └── viewmodel/     # UIのロジックと状態を管理するViewModel
├── build.gradle.kts           # プロジェクトレベルのビルドスクリプト
├── settings.gradle.kts        # プロジェクト設定（モジュールのインクルードなど）
└── gradle.properties          # Gradleのプロパティ設定
```

## 使用技術・ライブラリ
*   **言語:** Kotlin
*   **UIフレームワーク:** Jetpack Compose (Material 3)
*   **アーキテクチャ:** MVVM (Model-View-ViewModel)
*   **データベース:** Room (ローカル保存用)
*   **ナビゲーション:** Jetpack Navigation Compose
*   **ビルドシステム:** Gradle (Kotlin DSL)
