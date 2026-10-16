package com.example.exp9.ui.adapter

import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.exp9.R
import com.example.exp9.data.model.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(
    private var notes: List<Note> = emptyList(),
    var isCompactMode: Boolean = false,
    private val listener: OnNoteActionListener
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    interface OnNoteActionListener {
        fun onNoteClick(note: Note)
        fun onFavoriteToggle(note: Note)
        fun onPinToggle(note: Note)
        fun onArchiveToggle(note: Note)
        fun onDeleteClick(note: Note)
        fun onCompletedToggle(note: Note)
    }

    private val dateFormat = SimpleDateFormat("MMM d • h:mm a", Locale.getDefault())

    fun updateNotes(newNotes: List<Note>, compact: Boolean = isCompactMode) {
        this.notes = newNotes
        this.isCompactMode = compact
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (isCompactMode) VIEW_TYPE_COMPACT else VIEW_TYPE_COMFORTABLE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val layoutRes = if (viewType == VIEW_TYPE_COMPACT) {
            R.layout.item_note_compact
        } else {
            R.layout.item_note
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int = notes.size

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val btnFavorite: ImageView = itemView.findViewById(R.id.btnFavorite)
        private val btnPin: ImageView = itemView.findViewById(R.id.btnPin)
        private val btnComplete: ImageView = itemView.findViewById(R.id.btnComplete)

        // Comfortable mode specific views
        private val tvContent: TextView? = itemView.findViewById(R.id.tvContent)
        private val btnArchive: ImageView? = itemView.findViewById(R.id.btnArchive)
        private val btnDelete: ImageView? = itemView.findViewById(R.id.btnDelete)

        fun bind(note: Note) {
            tvTitle.text = note.title
            tvCategory.text = note.category
            tvDate.text = dateFormat.format(Date(note.createdAt))

            // Content snippet (if comfortable mode)
            tvContent?.text = note.content

            // Completed strikethrough styling
            if (note.isCompleted) {
                tvTitle.paintFlags = tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tvTitle.alpha = 0.6f
                btnComplete.setImageResource(R.drawable.ic_check_circle)
            } else {
                tvTitle.paintFlags = tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                tvTitle.alpha = 1.0f
                btnComplete.setImageResource(R.drawable.ic_circle_outline)
            }

            // Pinned state
            btnPin.visibility = if (note.isPinned) View.VISIBLE else View.GONE

            // Favorite state
            if (note.isFavorite) {
                btnFavorite.setImageResource(R.drawable.ic_favorite)
            } else {
                btnFavorite.setImageResource(R.drawable.ic_favorite_border)
            }

            // Archive state icon toggle
            if (note.isArchived) {
                btnArchive?.setImageResource(R.drawable.ic_unarchive)
            } else {
                btnArchive?.setImageResource(R.drawable.ic_archive)
            }

            // Category color badge
            applyCategoryStyle(tvCategory, note.category)

            // Click Listeners
            itemView.setOnClickListener { listener.onNoteClick(note) }
            btnFavorite.setOnClickListener { listener.onFavoriteToggle(note) }
            btnPin.setOnClickListener { listener.onPinToggle(note) }
            btnComplete.setOnClickListener { listener.onCompletedToggle(note) }
            btnArchive?.setOnClickListener { listener.onArchiveToggle(note) }
            btnDelete?.setOnClickListener { listener.onDeleteClick(note) }
        }

        private fun applyCategoryStyle(textView: TextView, category: String) {
            val context = textView.context
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
                setColor(ContextCompat.getColor(context, bgColor))
            }

            textView.background = drawable
            textView.setTextColor(ContextCompat.getColor(context, textColor))
        }
    }

    companion object {
        private const val VIEW_TYPE_COMFORTABLE = 1
        private const val VIEW_TYPE_COMPACT = 2
    }
}