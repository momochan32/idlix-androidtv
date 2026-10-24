package com.idlix.tv.ui.screens.watchlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.idlix.tv.data.local.WatchlistStore
import com.idlix.tv.data.model.WatchlistItem
import kotlinx.coroutines.flow.StateFlow

class WatchlistViewModel(app: Application) : AndroidViewModel(app) {

    private val store = WatchlistStore.getInstance(app)

    val items: StateFlow<List<WatchlistItem>> = store.itemsFlow

    fun remove(slug: String) = store.remove(slug)
}
