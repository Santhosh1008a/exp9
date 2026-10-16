package com.example.exp9.ui.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.exp9.R
import com.example.exp9.data.db.DatabaseHelper
import com.example.exp9.data.model.Note
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddEditNoteActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var existingNote: Note? = null
    private var isEditMode = false

    private lateinit var btnBack: ImageView
    private lateinit var tvScreenTitle: TextView
    private lateinit var btnSave: MaterialButton

    private lateinit var inputLayoutTitle: TextInputLayout
    private lateinit var etTitle: TextInputEditText

    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var chipPersonal: Chip
    private lateinit var chipStudy: Chip
    private lateinit var chipWork: Chip
    private lateinit var chipIdeas: Chip

    private lateinit var switchPinned: MaterialSwitch
    private lateinit var switchFavorite: MaterialSwitch

    private lateinit var inputLayoutContent: TextInputLayout
    private lateinit var etContent: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)

        dbHelper = DatabaseHelper(this)

        initViews()
        checkIntentData()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvScreenTitle = findViewById(R.id.tvScreenTitle)
        btnSave = findViewById(R.id.btnSave)

        inputLayoutTitle = findViewById(R.id.inputLayoutTitle)
        etTitle = findViewById(R.id.etTitle)

        chipGroupCategory = findViewById(R.id.chipGroupCategory)
        chipPersonal = findViewById(R.id.chipPersonal)
        chipStudy = findViewById(R.id.chipStudy)
        chipWork = findViewById(R.id.chipWork)
        chipIdeas = findViewById(R.id.chipIdeas)

        switchPinned = findViewById(R.id.switchPinned)
        switchFavorite = findViewById(R.id.switchFavorite)

        inputLayoutContent = findViewById(R.id.inputLayoutContent)
        etContent = findViewById(R.id.etContent)
    }

    private fun checkIntentData() {
        val noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)
        if (noteId != -1L) {
            isEditMode = true
            existingNote = dbHelper.getNoteById(noteId)
            existingNote?.let { populateFields(it) }
        }

        tvScreenTitle.text = getString(if (isEditMode) R.string.edit_note else R.string.add_note)
    }

    private fun populateFields(note: Note) {
        etTitle.setText(note.title)
        etContent.setText(note.content)
        switchPinned.isChecked = note.isPinned
        switchFavorite.isChecked = note.isFavorite

        when (note.category) {
            "Study" -> chipStudy.isChecked = true
            "Work" -> chipWork.isChecked = true
            "Ideas" -> chipIdeas.isChecked = true
            else -> chipPersonal.isChecked = true
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnSave.setOnClickListener { saveNote() }
    }

    private fun saveNote() {
        val title = etTitle.text?.toString()?.trim() ?: ""
        val content = etContent.text?.toString()?.trim() ?: ""

        var isValid = true

        if (title.isEmpty()) {
            inputLayoutTitle.error = getString(R.string.validation_title_required)
            isValid = false
        } else {
            inputLayoutTitle.error = null
        }

        if (content.isEmpty()) {
            inputLayoutContent.error = getString(R.string.validation_content_required)
            isValid = false
        } else {
            inputLayoutContent.error = null
        }

        if (!isValid) return

        val category = when (chipGroupCategory.checkedChipId) {
            R.id.chipStudy -> "Study"
            R.id.chipWork -> "Work"
            R.id.chipIdeas -> "Ideas"
            else -> "Personal"
        }

        val isPinned = switchPinned.isChecked
        val isFavorite = switchFavorite.isChecked

        if (isEditMode && existingNote != null) {
            val updatedNote = existingNote!!.copy(
                title = title,
                content = content,
                category = category,
                isPinned = isPinned,
                isFavorite = isFavorite,
                updatedAt = System.currentTimeMillis()
            )
            dbHelper.updateNote(updatedNote)
            Toast.makeText(this, R.string.note_updated, Toast.LENGTH_SHORT).show()
        } else {
            val newNote = Note(
                title = title,
                content = content,
                category = category,
                isPinned = isPinned,
                isFavorite = isFavorite,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            dbHelper.insertNote(newNote)
            Toast.makeText(this, R.string.note_saved, Toast.LENGTH_SHORT).show()
        }

        setResult(RESULT_OK)
        finish()
    }

    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
    }
}