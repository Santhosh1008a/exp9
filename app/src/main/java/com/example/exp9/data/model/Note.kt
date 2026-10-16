package com.example.exp9.data.model

import java.io.Serializable

data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "Personal",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isCompleted: Boolean = false
) : Serializable

data class NoteStats(
    val totalCount: Int = 0,
    val favoriteCount: Int = 0,
    val completedCount: Int = 0
)