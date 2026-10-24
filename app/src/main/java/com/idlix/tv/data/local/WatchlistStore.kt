package com.idlix.tv.data.local

import android.content.Context
import android.content.SharedPreferences
import com.idlix.tv.data.model.WatchlistItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WatchlistStore private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("idlix_tv_watchlist", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }

    private val _itemsFlow = MutableStateFlow<List<WatchlistItem>>(load())
    val itemsFlow: StateFlow<List<WatchlistItem>> = _itemsFlow.asStateFlow()

    private fun load(): List<WatchlistItem> {
        val raw = prefs.getString("watchlist_json", null) ?: return emptyList()
        return try { json.decodeFromString(raw) } catch (_: Exception) { emptyList() }
    }

    private fun save(items: List<WatchlistItem>) {
        prefs.edit().putString("watchlist_json", json.encodeToString(items)).apply()
        _itemsFlow.value = items
    }

    fun isInWatchlist(slug: String): Boolean = _itemsFlow.value.any { it.slug == slug }

    fun add(item: WatchlistItem) {
        if (isInWatchlist(item.slug)) return
        save(listOf(item) + _itemsFlow.value)
    }

    fun remove(slug: String) {
        save(_itemsFlow.value.filter { it.slug != slug })
    }

    fun toggle(item: WatchlistItem): Boolean {
        return if (isInWatchlist(item.slug)) {
            remove(item.slug)
            false
        } else {
            add(item)
            true
        }
    }

    fun getAll(): List<WatchlistItem> = _itemsFlow.value

    companion object {
        @Volatile private var INSTANCE: WatchlistStore? = null

        fun getInstance(context: Context): WatchlistStore =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: WatchlistStore(context.applicationContext).also { INSTANCE = it }
            }
    }
}
