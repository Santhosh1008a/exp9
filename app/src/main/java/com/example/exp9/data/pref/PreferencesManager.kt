package com.example.exp9.data.pref

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    var defaultSortOrder: String
        get() = prefs.getString(KEY_DEFAULT_SORT, SORT_NEWEST) ?: SORT_NEWEST
        set(value) = prefs.edit().putString(KEY_DEFAULT_SORT, value).apply()

    var defaultCategoryFilter: String
        get() = prefs.getString(KEY_DEFAULT_CATEGORY, CAT_ALL) ?: CAT_ALL
        set(value) = prefs.edit().putString(KEY_DEFAULT_CATEGORY, value).apply()

    var isCompactLayout: Boolean
        get() = prefs.getBoolean(KEY_IS_COMPACT, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_COMPACT, value).apply()

    var isFirstRun: Boolean
        get() = prefs.getBoolean(KEY_IS_FIRST_RUN, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_FIRST_RUN, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, " Alex") ?: " Alex"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    fun clearAllPreferences() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "memoflow_prefs"

        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_DEFAULT_SORT = "key_default_sort"
        private const val KEY_DEFAULT_CATEGORY = "key_default_category"
        private const val KEY_IS_COMPACT = "key_is_compact"
        private const val KEY_IS_FIRST_RUN = "key_is_first_run"
        private const val KEY_USER_NAME = "key_user_name"

        const val THEME_SYSTEM = "SYSTEM"
        const val THEME_LIGHT = "LIGHT"
        const val THEME_DARK = "DARK"

        const val SORT_NEWEST = "NEWEST"
        const val SORT_OLDEST = "OLDEST"
        const val SORT_TITLE = "TITLE"
        const val SORT_UPDATED = "UPDATED"

        const val CAT_ALL = "All"
    }
}