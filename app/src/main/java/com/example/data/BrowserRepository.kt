package com.example.data

import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val browserDao: BrowserDao) {

    val history: Flow<List<HistoryItem>> = browserDao.getHistory()
    val bookmarks: Flow<List<BookmarkItem>> = browserDao.getBookmarks()
    val tabs: Flow<List<TabItem>> = browserDao.getTabs()

    suspend fun saveHistory(url: String, title: String) {
        if (url.startsWith("data:") || url.startsWith("chrome-error:")) return
        val item = HistoryItem(url = url, title = title)
        browserDao.insertHistory(item)
    }

    suspend fun deleteHistory(id: Long) {
        browserDao.deleteHistoryItem(id)
    }

    suspend fun clearHistory() {
        browserDao.clearHistory()
    }

    suspend fun saveBookmark(url: String, title: String) {
        val item = BookmarkItem(url = url, title = title)
        browserDao.insertBookmark(item)
    }

    suspend fun removeBookmarkByUrl(url: String) {
        browserDao.deleteBookmarkByUrl(url)
    }

    suspend fun isBookmarked(url: String): Boolean {
        return browserDao.isBookmarked(url) > 0
    }

    suspend fun createTab(tab: TabItem) {
        browserDao.insertTab(tab)
    }

    suspend fun updateTab(tab: TabItem) {
        browserDao.updateTab(tab)
    }

    suspend fun deleteTab(tabId: String) {
        browserDao.deleteTab(tabId)
    }

    suspend fun clearAllTabs() {
        browserDao.clearAllTabs()
    }
}
