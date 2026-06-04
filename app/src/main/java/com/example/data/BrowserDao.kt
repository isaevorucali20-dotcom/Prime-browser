package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowserDao {
    // --- History Operations ---
    @Query("SELECT * FROM history_items ORDER BY timestamp DESC LIMIT 200")
    fun getHistory(): Flow<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryItem)

    @Query("DELETE FROM history_items WHERE id = :id")
    suspend fun deleteHistoryItem(id: Long)

    @Query("DELETE FROM history_items")
    suspend fun clearHistory()

    // --- Bookmark Operations ---
    @Query("SELECT * FROM bookmark_items ORDER BY timestamp DESC")
    fun getBookmarks(): Flow<List<BookmarkItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(item: BookmarkItem)

    @Query("DELETE FROM bookmark_items WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    @Query("SELECT COUNT(*) FROM bookmark_items WHERE url = :url")
    suspend fun isBookmarked(url: String): Int

    // --- Tab Operations ---
    @Query("SELECT * FROM tab_items ORDER BY lastVisited ASC")
    fun getTabs(): Flow<List<TabItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: TabItem)

    @Update
    suspend fun updateTab(tab: TabItem)

    @Query("DELETE FROM tab_items WHERE id = :tabId")
    suspend fun deleteTab(tabId: String)

    @Query("DELETE FROM tab_items")
    suspend fun clearAllTabs()
}
