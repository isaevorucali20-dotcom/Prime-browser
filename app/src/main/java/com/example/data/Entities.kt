package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_items")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmark_items")
data class BookmarkItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tab_items")
data class TabItem(
    @PrimaryKey val id: String, // UUID or custom unique ID
    val url: String,
    val title: String,
    val isDeveloperMode: Boolean = false,
    val lastVisited: Long = System.currentTimeMillis()
)
