package com.idlix.tv.ui.screens.series

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Text
import com.idlix.tv.R
import com.idlix.tv.ui.components.TvHeroBillboard
import com.idlix.tv.ui.components.TvLoadingSpinner
import com.idlix.tv.ui.components.TvSwimlaneRow
import com.idlix.tv.ui.theme.NetflixBlack
import com.idlix.tv.ui.theme.NetflixWhite

@Composable
fun SeriesScreen(
    onNavigateToDetail: (contentType: String, slug: String) -> Unit,
    onNavigateToPlayer: (contentType: String, contentId: String, title: String, poster: String?, backdrop: String?, slug: String?) -> Unit,
    onNavigateToCategory: ((title: String, contentType: String, filterType: String, filterValue: String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: SeriesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
    ) {
        if (uiState.isLoading) {
            TvLoadingSpinner(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (uiState.error != null) {
            Text(
                text = uiState.error ?: "",
                color = NetflixWhite,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Hero TV Series Billboard
                item {
                    TvHeroBillboard(
                        movie = uiState.heroSeries,
                        onPlayClick = {
                            uiState.heroSeries?.let {
                                onNavigateToDetail(it.contentType, it.slug)
                            }
                        },
                        onDetailsClick = {
                            uiState.heroSeries?.let {
                                onNavigateToDetail(it.contentType, it.slug)
                            }
                        }
                    )
                }

                // Swimlane: Serial TV Terpopuler
                if (uiState.popularSeries.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.popular_series)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.popularSeries,
                            onSeeMoreClick = onNavigateToCategory?.let { nav ->
                                { nav(title, "tv_series", "sort", "popularity") }
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // Swimlane: Serial TV Terbaru
                if (uiState.latestSeries.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.on_the_air_series)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.latestSeries,
                            onSeeMoreClick = onNavigateToCategory?.let { nav ->
                                { nav(title, "tv_series", "sort", "createdAt") }
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // Swimlane: Serial Drama
                if (uiState.dramaSeries.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.series_drama)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.dramaSeries,
                            onSeeMoreClick = onNavigateToCategory?.let { nav ->
                                { nav(title, "tv_series", "genre", "drama") }
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // Swimlane: Serial Aksi & Petualangan
                if (uiState.actionSeries.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.series_action)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.actionSeries,
                            onSeeMoreClick = onNavigateToCategory?.let { nav ->
                                { nav(title, "tv_series", "genre", "action") }
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // Swimlane: Serial Komedi
                if (uiState.comedySeries.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.series_comedy)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.comedySeries,
                            onSeeMoreClick = onNavigateToCategory?.let { nav ->
                                { nav(title, "tv_series", "genre", "comedy") }
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }
            }
        }
    }
}
