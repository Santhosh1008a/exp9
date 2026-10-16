package com.example.exp9.ui.activity

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.exp9.R
import com.example.exp9.data.db.DatabaseHelper
import com.example.exp9.data.model.Note
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var noteId: Long = -1L
    private var currentNote: Note? = null

    private lateinit var btnBack: ImageView
    private lateinit var btnFavorite: ImageView
    private lateinit var btnEdit: ImageView
    private lateinit var btnDelete: ImageView

    private lateinit var tvCategory: TextView
    private lateinit var badgePinned: TextView
    private lateinit var badgeCompleted: TextView

    private lateinit var tvTitle: TextView
    private lateinit var tvCreatedAt: TextView
    private lateinit var tvUpdatedAt: TextView
    private lateinit var tvContent: TextView

    private lateinit var btnToggleComplete: MaterialButton
    private lateinit var btnToggleArchive: MaterialButton

    private val fullDateFormat = SimpleDateFormat("EEE, MMM d, yyyy 'at' h:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        dbHelper = DatabaseHelper(this)
        noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1L)

        if (noteId == -1L) {
            finish()
            return
        }

        initViews()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadNoteDetails()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnFavorite = findViewById(R.id.btnFavorite)
        btnEdit = findViewById(R.id.btnEdit)
        btnDelete = findViewById(R.id.btnDelete)

        tvCategory = findViewById(R.id.tvCategory)
        badgePinned = findViewById(R.id.badgePinned)
        badgeCompleted = findViewById(R.id.badgeCompleted)

        tvTitle = findViewById(R.id.tvTitle)
        tvCreatedAt = findViewById(R.id.tvCreatedAt)
        tvUpdatedAt = findViewById(R.id.tvUpdatedAt)
        tvContent = findViewById(R.id.tvContent)

        btnToggleComplete = findViewById(R.id.btnToggleComplete)
        btnToggleArchive = findViewById(R.id.btnToggleArchive)
    }

    private fun loadNoteDetails() {
        currentNote = dbHelper.getNoteById(noteId)
        val note = currentNote ?: run {
            finish()
            return
        }

        tvTitle.text = note.title
        tvContent.text = note.content
        tvCategory.text = note.category

        tvCreatedAt.text = getString(R.string.created_at, fullDateFormat.format(Date(note.createdAt)))
        tvUpdatedAt.text = getString(R.string.updated_at, fullDateFormat.format(Date(note.updatedAt)))

        // Badges
        badgePinned.visibility = if (note.isPinned) View.VISIBLE else View.GONE
        badgeCompleted.visibility = if (note.isCompleted) View.VISIBLE else View.GONE

        // Favorite icon
        btnFavorite.setImageResource(
            if (note.isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )

        // Complete toggle button state
        if (note.isCompleted) {
            btnToggleComplete.setText(R.string.mark_pending)
            btnToggleComplete.setIconResource(R.drawable.ic_circle_outline)
        } else {
            btnToggleComplete.setText(R.string.mark_completed)
            btnToggleComplete.setIconResource(R.drawable.ic_check_circle)
        }

        // Archive toggle button state
        if (note.isArchived) {
            btnToggleArchive.setText(R.string.unarchive)
            btnToggleArchive.setIconResource(R.drawable.ic_unarchive)
        } else {
            btnToggleArchive.setText(R.string.archive)
            btnToggleArchive.setIconResource(R.drawable.ic_archive)
        }

        applyCategoryStyle(tvCategory, note.category)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnFavorite.setOnClickListener {
            currentNote?.let { note ->
                val newFavState = !note.isFavorite
                dbHelper.toggleFavorite(note.id, newFavState)
                loadNoteDetails()
            }
        }

        btnEdit.setOnClickListener {
            val intent = Intent(this, AddEditNoteActivity::class.java).apply {
                putExtra(AddEditNoteActivity.EXTRA_NOTE_ID, noteId)
            }
            startActivity(intent)
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_msg)
                .setPositiveButton(R.string.delete) { _, _ ->
                    dbHelper.deleteNote(noteId)
                    Toast.makeText(this, R.string.note_deleted, Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        btnToggleComplete.setOnClickListener {
            currentNote?.let { note ->
                val newCompState = !note.isCompleted
                dbHelper.toggleCompleted(note.id, newCompState)
                loadNoteDetails()
            }
        }

        btnToggleArchive.setOnClickListener {
            currentNote?.let { note ->
                val newArchiveState = !note.isArchived
                dbHelper.toggleArchive(note.id, newArchiveState)
                val msgRes = if (newArchiveState) R.string.note_archived else R.string.note_unarchived
                Toast.makeText(this, msgRes, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun applyCategoryStyle(textView: TextView, category: String) {
        val (bgColor, textColor) = when (category) {
            "Personal" -> R.color.cat_personal_bg to R.color.cat_personal_text
            "Study" -> R.color.cat_study_bg to R.color.cat_study_text
            "Work" -> R.color.cat_work_bg to R.color.cat_work_text
            "Ideas" -> R.color.cat_ideas_bg to R.color.cat_ideas_text
            else -> R.color.cat_default_bg to R.color.cat_default_text
        }

        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f
            setColor(ContextCompat.getColor(this@NoteDetailActivity, bgColor))
        }

        textView.background = drawable
        textView.setTextColor(ContextCompat.getColor(this@NoteDetailActivity, textColor))
    }

    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
    }
}