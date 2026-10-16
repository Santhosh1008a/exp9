package com.example.exp9.ui.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.exp9.R
import com.example.exp9.data.db.DatabaseHelper
import com.example.exp9.data.pref.PreferencesManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager
    private lateinit var dbHelper: DatabaseHelper

    private lateinit var btnBack: ImageView
    private lateinit var rgThemeMode: RadioGroup
    private lateinit var rbThemeSystem: RadioButton
    private lateinit var rbThemeLight: RadioButton
    private lateinit var rbThemeDark: RadioButton

    private lateinit var switchCompactLayout: MaterialSwitch

    private lateinit var rgDefaultSort: RadioGroup
    private lateinit var rbSortNewest: RadioButton
    private lateinit var rbSortOldest: RadioButton
    private lateinit var rbSortTitle: RadioButton
    private lateinit var rbSortUpdated: RadioButton

    private lateinit var btnLoadSamples: MaterialButton
    private lateinit var btnClearAll: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefsManager = PreferencesManager(this)
        dbHelper = DatabaseHelper(this)

        setContentView(R.layout.activity_settings)

        initViews()
        loadSavedPreferences()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        rgThemeMode = findViewById(R.id.rgThemeMode)
        rbThemeSystem = findViewById(R.id.rbThemeSystem)
        rbThemeLight = findViewById(R.id.rbThemeLight)
        rbThemeDark = findViewById(R.id.rbThemeDark)

        switchCompactLayout = findViewById(R.id.switchCompactLayout)

        rgDefaultSort = findViewById(R.id.rgDefaultSort)
        rbSortNewest = findViewById(R.id.rbSortNewest)
        rbSortOldest = findViewById(R.id.rbSortOldest)
        rbSortTitle = findViewById(R.id.rbSortTitle)
        rbSortUpdated = findViewById(R.id.rbSortUpdated)

        btnLoadSamples = findViewById(R.id.btnLoadSamples)
        btnClearAll = findViewById(R.id.btnClearAll)
    }

    private fun loadSavedPreferences() {
        // Theme selection state
        when (prefsManager.themeMode) {
            PreferencesManager.THEME_LIGHT -> rbThemeLight.isChecked = true
            PreferencesManager.THEME_DARK -> rbThemeDark.isChecked = true
            else -> rbThemeSystem.isChecked = true
        }

        // Layout density state
        switchCompactLayout.isChecked = prefsManager.isCompactLayout

        // Default sort state
        when (prefsManager.defaultSortOrder) {
            PreferencesManager.SORT_OLDEST -> rbSortOldest.isChecked = true
            PreferencesManager.SORT_TITLE -> rbSortTitle.isChecked = true
            PreferencesManager.SORT_UPDATED -> rbSortUpdated.isChecked = true
            else -> rbSortNewest.isChecked = true
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        rgThemeMode.setOnCheckedChangeListener { _, checkedId ->
            val newTheme = when (checkedId) {
                R.id.rbThemeLight -> PreferencesManager.THEME_LIGHT
                R.id.rbThemeDark -> PreferencesManager.THEME_DARK
                else -> PreferencesManager.THEME_SYSTEM
            }
            prefsManager.themeMode = newTheme
            applyTheme(newTheme)
        }

        switchCompactLayout.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.isCompactLayout = isChecked
        }

        rgDefaultSort.setOnCheckedChangeListener { _, checkedId ->
            val newSort = when (checkedId) {
                R.id.rbSortOldest -> PreferencesManager.SORT_OLDEST
                R.id.rbSortTitle -> PreferencesManager.SORT_TITLE
                R.id.rbSortUpdated -> PreferencesManager.SORT_UPDATED
                else -> PreferencesManager.SORT_NEWEST
            }
            prefsManager.defaultSortOrder = newSort
        }

        btnLoadSamples.setOnClickListener {
            dbHelper.insertSampleNotes()
            Toast.makeText(this, R.string.samples_added, Toast.LENGTH_SHORT).show()
        }

        btnClearAll.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_clear_data_msg)
                .setPositiveButton(R.string.delete) { _, _ ->
                    dbHelper.deleteAllNotes()
                    Toast.makeText(this, R.string.all_data_cleared, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun applyTheme(themeMode: String) {
        val nightMode = when (themeMode) {
            PreferencesManager.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            PreferencesManager.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}