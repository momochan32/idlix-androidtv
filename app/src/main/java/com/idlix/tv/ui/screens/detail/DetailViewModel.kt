package com.idlix.tv.ui.screens.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.idlix.tv.data.local.WatchHistoryStore
import com.idlix.tv.data.local.WatchlistStore
import com.idlix.tv.data.model.EpisodeItem
import com.idlix.tv.data.model.MovieDetail
import com.idlix.tv.data.model.Season
import com.idlix.tv.data.model.SeriesDetail
import com.idlix.tv.data.model.WatchHistoryItem
import com.idlix.tv.data.model.WatchlistItem
import com.idlix.tv.data.repository.IdlixRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val movie: MovieDetail? = null,
    val series: SeriesDetail? = null,
    val selectedSeason: Season? = null,
    val episodes: List<EpisodeItem> = emptyList(),
    val error: String? = null,
    val isInWatchlist: Boolean = false,
    val watchHistoryItem: WatchHistoryItem? = null
)

class DetailViewModel(
    app: Application,
    private val repository: IdlixRepository = IdlixRepository()
) : AndroidViewModel(app) {

    private val watchlistStore = WatchlistStore.getInstance(app)
    private val historyStore = WatchHistoryStore.getInstance(app)

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadDetail(contentType: String, slug: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val inWatchlist = watchlistStore.isInWatchlist(slug)
                if (contentType == "tv_series") {
                    val series = repository.getSeriesDetail(slug)
                    val firstSeason = series?.seasons?.firstOrNull()
                    val episodes = series?.defaultSeason?.episodes ?: emptyList()
                    val historyItem = historyStore.getAllHistory()
                        .firstOrNull { it.slug == slug || it.contentId == slug }

                    _uiState.value = DetailUiState(
                        isLoading = false,
                        series = series,
                        selectedSeason = firstSeason,
                        episodes = episodes,
                        isInWatchlist = inWatchlist,
                        watchHistoryItem = historyItem
                    )
                } else {
                    val movie = repository.getMovieDetail(slug)
                    val historyItem = historyStore.getAllHistory()
                        .firstOrNull { it.slug == slug || it.contentId == movie?.id }

                    _uiState.value = DetailUiState(
                        isLoading = false,
                        movie = movie,
                        isInWatchlist = inWatchlist,
                        watchHistoryItem = historyItem
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Gagal memuat detail"
                )
            }
        }
    }

    fun toggleWatchlist(
        slug: String,
        contentType: String,
        title: String,
        posterUrl: String?,
        backdropUrl: String?
    ) {
        val item = WatchlistItem(
            slug = slug,
            contentType = contentType,
            title = title,
            posterUrl = posterUrl,
            backdropUrl = backdropUrl
        )
        val nowInWatchlist = watchlistStore.toggle(item)
        _uiState.value = _uiState.value.copy(isInWatchlist = nowInWatchlist)
    }

    fun selectSeason(seriesSlug: String, season: Season) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedSeason = season)
            try {
                // Fetch specific season episodes via episode 1 detail
                val epRes = repository.getEpisodeDetail(seriesSlug, season.seasonNumber, 1)
                val newEpisodes = epRes?.season?.episodes ?: emptyList()
                if (newEpisodes.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(episodes = newEpisodes)
                }
            } catch (e: Exception) {
                // Keep existing episodes if failure
            }
        }
    }
}
