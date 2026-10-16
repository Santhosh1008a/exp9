package com.example.exp9

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.exp9.data.db.DatabaseHelper
import com.example.exp9.data.model.Note
import com.example.exp9.data.pref.PreferencesManager
import com.example.exp9.ui.activity.AddEditNoteActivity
import com.example.exp9.ui.activity.NoteDetailActivity
import com.example.exp9.ui.activity.SettingsActivity
import com.example.exp9.ui.adapter.NoteAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), NoteAdapter.OnNoteActionListener {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var prefsManager: PreferencesManager
    private lateinit var noteAdapter: NoteAdapter

    // Views
    private lateinit var tvGreeting: TextView
    private lateinit var btnQuickSettings: ImageView

    private lateinit var tvStatTotal: TextView
    private lateinit var tvStatFavorites: TextView
    private lateinit var tvStatCompleted: TextView

    private lateinit var etSearch: EditText
    private lateinit var btnClearSearch: ImageView

    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var chipAll: Chip

    private lateinit var rvNotes: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptySub: TextView
    private lateinit var btnEmptyAction: MaterialButton

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fabAddNote: FloatingActionButton

    // Filter states
    private var currentTabId = R.id.nav_notes
    private var currentSearchQuery = ""
    private var currentCategory = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefsManager = PreferencesManager(this)
        dbHelper = DatabaseHelper(this)

        applySavedTheme()

        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        checkFirstRun()
        initViews()
        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        applySavedTheme()
        loadData()
    }

    private fun applySavedTheme() {
        val nightMode = when (prefsManager.themeMode) {
            PreferencesManager.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            PreferencesManager.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun checkFirstRun() {
        if (prefsManager.isFirstRun) {
            dbHelper.insertSampleNotes()
            prefsManager.isFirstRun = false
        }
    }

    private fun initViews() {
        tvGreeting = findViewById(R.id.tvGreeting)
        btnQuickSettings = findViewById(R.id.btnQuickSettings)

        tvStatTotal = findViewById(R.id.tvStatTotal)
        tvStatFavorites = findViewById(R.id.tvStatFavorites)
        tvStatCompleted = findViewById(R.id.tvStatCompleted)

        etSearch = findViewById(R.id.etSearch)
        btnClearSearch = findViewById(R.id.btnClearSearch)

        chipGroupCategory = findViewById(R.id.chipGroupCategory)
        chipAll = findViewById(R.id.chipAll)

        rvNotes = findViewById(R.id.rvNotes)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle)
        tvEmptySub = findViewById(R.id.tvEmptySub)
        btnEmptyAction = findViewById(R.id.btnEmptyAction)

        bottomNavigation = findViewById(R.id.bottomNavigation)
        fabAddNote = findViewById(R.id.fabAddNote)

        tvGreeting.text = getString(R.string.greeting_default)
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            isCompactMode = prefsManager.isCompactLayout,
            listener = this
        )
        rvNotes.layoutManager = LinearLayoutManager(this)
        rvNotes.adapter = noteAdapter
    }

    private fun setupListeners() {
        btnQuickSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        fabAddNote.setOnClickListener {
            startActivity(Intent(this, AddEditNoteActivity::class.java))
        }

        btnEmptyAction.setOnClickListener {
            startActivity(Intent(this, AddEditNoteActivity::class.java))
        }

        // Search text watcher
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                btnClearSearch.visibility = if (currentSearchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                loadData()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnClearSearch.setOnClickListener {
            etSearch.setText("")
        }

        // Category chip group selection listener
        chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                chipAll.isChecked = true
                currentCategory = "All"
            } else {
                val chip = group.findViewById<Chip>(checkedIds[0])
                currentCategory = chip?.text?.toString() ?: "All"
            }
            loadData()
        }

        // Bottom navigation tab listener
        bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.nav_settings) {
                startActivity(Intent(this, SettingsActivity::class.java))
                return@setOnItemSelectedListener false
            }

            currentTabId = item.itemId
            loadData()
            true
        }
    }

    private fun loadData() {
        // Update stats counters
        val stats = dbHelper.getStats()
        tvStatTotal.text = stats.totalCount.toString()
        tvStatFavorites.text = stats.favoriteCount.toString()
        tvStatCompleted.text = stats.completedCount.toString()

        val showArchived = (currentTabId == R.id.nav_archive)
        val showFavoritesOnly = (currentTabId == R.id.nav_favorites)

        val notes = dbHelper.getAllNotes(
            searchQuery = currentSearchQuery,
            category = currentCategory,
            sortOrder = prefsManager.defaultSortOrder,
            showArchived = showArchived,
            showFavoritesOnly = showFavoritesOnly
        )

        val isCompact = prefsManager.isCompactLayout
        noteAdapter.updateNotes(notes, isCompact)

        // Empty state visibility
        if (notes.isEmpty()) {
            rvNotes.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
            updateEmptyStateText()
        } else {
            rvNotes.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
        }
    }

    private fun updateEmptyStateText() {
        when (currentTabId) {
            R.id.nav_favorites -> {
                tvEmptyTitle.setText(R.string.empty_favorites_title)
                tvEmptySub.setText(R.string.empty_favorites_sub)
                btnEmptyAction.visibility = View.GONE
            }
            R.id.nav_archive -> {
                tvEmptyTitle.setText(R.string.empty_archive_title)
                tvEmptySub.setText(R.string.empty_archive_sub)
                btnEmptyAction.visibility = View.GONE
            }
            else -> {
                tvEmptyTitle.setText(R.string.empty_notes_title)
                tvEmptySub.setText(R.string.empty_notes_sub)
                btnEmptyAction.visibility = View.VISIBLE
            }
        }
    }

    // NoteAdapter Callbacks
    override fun onNoteClick(note: Note) {
        val intent = Intent(this, NoteDetailActivity::class.java).apply {
            putExtra(NoteDetailActivity.EXTRA_NOTE_ID, note.id)
        }
        startActivity(intent)
    }

    override fun onFavoriteToggle(note: Note) {
        dbHelper.toggleFavorite(note.id, !note.isFavorite)
        loadData()
    }

    override fun onPinToggle(note: Note) {
        dbHelper.togglePin(note.id, !note.isPinned)
        loadData()
    }

    override fun onArchiveToggle(note: Note) {
        dbHelper.toggleArchive(note.id, !note.isArchived)
        val msgRes = if (!note.isArchived) R.string.note_archived else R.string.note_unarchived
        Toast.makeText(this, msgRes, Toast.LENGTH_SHORT).show()
        loadData()
    }

    override fun onDeleteClick(note: Note) {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_msg)
            .setPositiveButton(R.string.delete) { _, _ ->
                dbHelper.deleteNote(note.id)
                Toast.makeText(this, R.string.note_deleted, Toast.LENGTH_SHORT).show()
                loadData()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onCompletedToggle(note: Note) {
        dbHelper.toggleCompleted(note.id, !note.isCompleted)
        loadData()
    }
}