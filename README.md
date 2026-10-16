# MemoFlow — Personal Notes & Productivity

**MemoFlow** is a modern, polished Android productivity application built with Kotlin and Material Design 3. It demonstrates dual local data persistence mechanisms using **SharedPreferences** for user preferences and lightweight app settings, and **SQLite (`SQLiteOpenHelper`)** for full offline notes CRUD management.

---

## 🌟 Key Features

- 📝 **Full Notes Management (SQLite CRUD)**: Create, view, edit, and delete notes with automatic timestamps (`created_at` & `updated_at`).
- 📌 **Note Organization & Statuses**: Pin important notes to the top, mark favorites, complete task items, and archive inactive notes.
- 📊 **Real-time Dashboard Statistics**: Instant visual counters for total notes, favorites, and completed items.
- 🔍 **Instant Search & Filtering**: Fast text search across titles and content, combined with category filter chips (`Personal`, `Study`, `Work`, `Ideas`).
- 🎨 **Appearance Customization (SharedPreferences)**: Support for Light, Dark, and System Default themes, plus toggleable Comfortable Card vs. Compact List views.
- ⚙️ **Persistent Preferences**: Default sort order (Newest, Oldest, Title A-Z, Recently Updated) and display density preferences restored automatically upon launch.
- 🚀 **Sample Data Generator**: Onboarding sample notes auto-populated on first launch with quick-reset options in Settings.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Native XML Layouts with Material Design 3 Components (`MaterialCardView`, `ChipGroup`, `MaterialSwitch`, `BottomNavigationView`, `FloatingActionButton`, `CoordinatorLayout`)
- **Persistence Engine 1**: `SharedPreferences` via `PreferencesManager.kt`
- **Persistence Engine 2**: `SQLiteOpenHelper` via `DatabaseHelper.kt`
- **Component Pattern**: Activities & Intents (`MainActivity`, `AddEditNoteActivity`, `NoteDetailActivity`, `SettingsActivity`)
- **List Rendering**: `RecyclerView` with `NoteAdapter` supporting dual layout view types

---

## 💾 Storage Architecture Details

### 1. SQLite Database Schema (`memoflow.db`)

Table Name: `notes`

| Column | Data Type | Constraint / Default | Description |
| :--- | :--- | :--- | :--- |
| `_id` | `INTEGER` | `PRIMARY KEY AUTOINCREMENT` | Unique note identifier |
| `title` | `TEXT` | `NOT NULL` | Note title |
| `content` | `TEXT` | `NOT NULL` | Note body content |
| `category` | `TEXT` | `NOT NULL` | Category tag (`Personal`, `Study`, `Work`, `Ideas`) |
| `created_at` | `INTEGER` | `NOT NULL` | Epoch timestamp in milliseconds |
| `updated_at` | `INTEGER` | `NOT NULL` | Epoch timestamp in milliseconds |
| `is_favorite` | `INTEGER` | `NOT NULL DEFAULT 0` | 1 = Favorite, 0 = Normal |
| `is_pinned` | `INTEGER` | `NOT NULL DEFAULT 0` | 1 = Pinned to top, 0 = Normal |
| `is_archived` | `INTEGER` | `NOT NULL DEFAULT 0` | 1 = Archived, 0 = Active |
| `is_completed` | `INTEGER` | `NOT NULL DEFAULT 0` | 1 = Completed, 0 = Pending |

### 2. SharedPreferences Registry (`memoflow_prefs`)

- `key_theme_mode`: App theme configuration (`SYSTEM`, `LIGHT`, `DARK`)
- `key_default_sort`: Preferred note sorting order (`NEWEST`, `OLDEST`, `TITLE`, `UPDATED`)
- `key_default_category`: Selected default category filter
- `key_is_compact`: Display view density flag (`true` for Compact list, `false` for Comfortable cards)
- `key_is_first_run`: Boolean flag triggering sample data insertion on initial startup

---

## 📁 Project Structure

```text
com.example.exp9
├── MainActivity.kt                  # Dashboard, search, filtering, bottom navigation
├── data/
│   ├── db/
│   │   └── DatabaseHelper.kt        # SQLiteOpenHelper implementation
│   ├── model/
│   │   └── Note.kt                  # Note data class & NoteStats model
│   └── pref/
│       └── PreferencesManager.kt    # SharedPreferences manager
└── ui/
    ├── activity/
    │   ├── AddEditNoteActivity.kt   # Create and modify notes with validation
    │   ├── NoteDetailActivity.kt    # View note details & quick actions
    │   └── SettingsActivity.kt      # Appearance & database management
    └── adapter/
        └── NoteAdapter.kt           # RecyclerView adapter for note cards
```

---

## 🚀 Building & Running the App

1. Clone or download the repository:
   ```bash
   git clone https://github.com/Santhosh1008a/exp9.git
   ```
2. Open the project folder in **Android Studio**.
3. Let Gradle sync dependencies.
4. Select an Android emulator or connected device running Android 7.0 (API 24) or higher.
5. Click **Run (`Shift + F10`)** or execute Gradle build:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🎓 Academic / Practical Demonstration Highlights

This project was built for an Android Development Practical Demonstration (Experiment 9):
1. **SharedPreferences**: Demonstrates storing non-relational user preferences that survive app restarts.
2. **SQLite Database**: Demonstrates relational database setup, table creation, CRUD operations, parameterized SQL queries, cursor parsing, and statistics aggregation using native `SQLiteOpenHelper`.
