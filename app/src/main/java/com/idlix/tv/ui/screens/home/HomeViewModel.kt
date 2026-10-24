package com.idlix.tv.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.idlix.tv.data.local.WatchHistoryStore
import com.idlix.tv.data.model.MovieItem
import com.idlix.tv.data.model.SeriesItem
import com.idlix.tv.data.model.WatchHistoryItem
import com.idlix.tv.data.repository.IdlixRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val heroMovie: MovieItem? = null,
    val continueWatching: List<WatchHistoryItem> = emptyList(),
    val trending: List<MovieItem> = emptyList(),
    val popularMovies: List<MovieItem> = emptyList(),
    val latestMovies: List<MovieItem> = emptyList(),
    val popularSeries: List<SeriesItem> = emptyList(),
    val latestSeries: List<SeriesItem> = emptyList(),
    val actionMovies: List<MovieItem> = emptyList(),
    val comedyMovies: List<MovieItem> = emptyList(),
    val horrorMovies: List<MovieItem> = emptyList(),
    val animationMovies: List<MovieItem> = emptyList(),
    val sciFiMovies: List<MovieItem> = emptyList(),
    val romanceMovies: List<MovieItem> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = IdlixRepository()
    private val historyStore = WatchHistoryStore.getInstance(application.applicationContext)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Collect unfinished watch history items reactively
        viewModelScope.launch {
            historyStore.historyFlow.collect { allItems ->
                val unfinished = allItems.filter { it.isUnfinished }
                _uiState.value = _uiState.value.copy(continueWatching = unfinished)
            }
        }
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Phase 1: Load essential top-of-screen content first for instantaneous display
                val trendingDeferred = async { repository.getTrendingMovies() }
                val popularDeferred = async { repository.getPopularMovies() }
                val seriesDeferred = async { repository.getPopularSeries() }
                val unfinished = historyStore.getUnfinishedItems()

                val trending = trendingDeferred.await()
                val popular = popularDeferred.await()
                val series = seriesDeferred.await()
                val hero = trending.firstOrNull() ?: popular.firstOrNull()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    heroMovie = hero,
                    continueWatching = unfinished,
                    trending = trending,
                    popularMovies = popular,
                    popularSeries = series
                )

                // Phase 2: Concurrently fetch expanded genres and categories to enrich Home
                val latestMoviesDef = async { repository.getMoviesBySort("createdAt") }
                val latestSeriesDef = async { repository.getSeriesBySort("createdAt") }
                val actionDef = async { repository.getMoviesByGenre("action") }
                val comedyDef = async { repository.getMoviesByGenre("comedy") }
                val horrorDef = async { repository.getMoviesByGenre("horror") }
                val animationDef = async { repository.getMoviesByGenre("animation") }
                val sciFiDef = async { repository.getMoviesByGenre("science-fiction") }
                val romanceDef = async { repository.getMoviesByGenre("romance") }

                val latestMovies = latestMoviesDef.await()
                val latestSeries = latestSeriesDef.await()
                val action = actionDef.await()
                val comedy = comedyDef.await()
                val horror = horrorDef.await()
                val animation = animationDef.await()
                val sciFi = sciFiDef.await()
                val romance = romanceDef.await()

                _uiState.value = _uiState.value.copy(
                    latestMovies = latestMovies,
                    latestSeries = latestSeries,
                    actionMovies = action,
                    comedyMovies = comedy,
                    horrorMovies = horror,
                    animationMovies = animation,
                    sciFiMovies = sciFi,
                    romanceMovies = romance
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = if (_uiState.value.trending.isEmpty() && _uiState.value.popularMovies.isEmpty()) {
                        e.localizedMessage ?: "Terjadi kesalahan saat memuat data"
                    } else null
                )
            }
        }
    }

    fun refreshContinueWatching() {
        val unfinished = historyStore.getUnfinishedItems()
        _uiState.value = _uiState.value.copy(continueWatching = unfinished)
    }

    fun removeFromHistory(contentId: String) {
        historyStore.removeItem(contentId)
        refreshContinueWatching()
    }

    fun setHeroMovie(movie: MovieItem) {
        _uiState.value = _uiState.value.copy(heroMovie = movie)
    }
}
