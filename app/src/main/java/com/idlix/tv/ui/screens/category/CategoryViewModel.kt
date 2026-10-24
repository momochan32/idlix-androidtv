package com.idlix.tv.ui.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idlix.tv.data.model.MovieItem
import com.idlix.tv.data.repository.IdlixRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryUiState(
    val title: String = "",
    val contentType: String = "movie",
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val items: List<MovieItem> = emptyList(),
    val page: Int = 1,
    val hasMore: Boolean = true,
    val error: String? = null
)

class CategoryViewModel(
    private val repository: IdlixRepository = IdlixRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    private var currentTitle = ""
    private var currentType = "movie"
    private var currentFilterType = "sort"
    private var currentFilterValue = ""

    fun init(title: String, contentType: String, filterType: String, filterValue: String) {
        if (currentTitle == title && currentType == contentType && currentFilterValue == filterValue && _uiState.value.items.isNotEmpty()) {
            return
        }
        currentTitle = title
        currentType = contentType
        currentFilterType = filterType
        currentFilterValue = filterValue

        _uiState.value = CategoryUiState(
            title = title,
            contentType = contentType,
            isLoading = true,
            page = 1,
            hasMore = true
        )
        loadPage(1)
    }

    private fun loadPage(page: Int) {
        viewModelScope.launch {
            try {
                val sort = if (currentFilterType == "sort") currentFilterValue.ifBlank { null } else null
                val genre = if (currentFilterType == "genre") currentFilterValue.ifBlank { null } else null

                val newItems: List<MovieItem> = if (currentType == "tv_series") {
                    repository.getSeriesPaginated(page = page, limit = 24, sort = sort, genre = genre)
                        .map { it.toMovieItem() }
                } else {
                    repository.getMoviesPaginated(page = page, limit = 24, sort = sort, genre = genre)
                }

                val existing = if (page == 1) emptyList() else _uiState.value.items
                val combined = existing + newItems
                val hasMore = newItems.isNotEmpty() && newItems.size >= 12

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    items = combined,
                    page = page,
                    hasMore = hasMore,
                    error = if (combined.isEmpty()) "Tidak ada judul yang ditemukan." else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = e.localizedMessage ?: "Gagal memuat kategori"
                )
            }
        }
    }

    fun loadNextPage() {
        if (_uiState.value.isLoading || _uiState.value.isLoadingMore || !_uiState.value.hasMore) {
            return
        }
        _uiState.value = _uiState.value.copy(isLoadingMore = true)
        loadPage(_uiState.value.page + 1)
    }
}
