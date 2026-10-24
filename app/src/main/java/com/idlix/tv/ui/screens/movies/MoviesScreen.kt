package com.idlix.tv.ui.screens.movies

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
fun MoviesScreen(
    onNavigateToDetail: (contentType: String, slug: String) -> Unit,
    onNavigateToPlayer: (contentType: String, contentId: String, title: String, poster: String?, backdrop: String?, slug: String?) -> Unit,
    onNavigateToCategory: ((title: String, contentType: String, filterType: String, filterValue: String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: MoviesViewModel = viewModel()
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
                // Hero Movie Billboard
                item {
                    TvHeroBillboard(
                        movie = uiState.heroMovie,
                        onPlayClick = {
                            uiState.heroMovie?.let {
                                onNavigateToPlayer(it.contentType, it.id, it.title, it.fullPosterUrl, it.fullBackdropUrl, it.slug)
                            }
                        },
                        onDetailsClick = {
                            uiState.heroMovie?.let {
                                onNavigateToDetail(it.contentType, it.slug)
                            }
                        }
                    )
                }

                // Swimlane: Film Terpopuler
                if (uiState.popularMovies.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.popular_movies_indonesia)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.popularMovies,
                            onSeeMoreClick = onNavigateToCategory?.let { nav ->
                                { nav(title, "movie", "sort", "") }
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // Swimlane: Film Terbaru
                if (uiState.latestMovies.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.latest_movies)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.latestMovies,
                            onSeeMoreClick = onNavigateToCategory?.let { nav ->
                                { nav(title, "movie", "sort", "createdAt") }
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // Swimlane: Film Aksi
                if (uiState.actionMovies.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.movies_action)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.actionMovies,
                            onSeeMoreClick = onNavigateToCategory?.let { nav ->
                                { nav(title, "movie", "genre", "action") }
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // Swimlane: Film Komedi
                if (uiState.comedyMovies.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.movies_comedy)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.comedyMovies,
                            onSeeMoreClick = onNavigateToCategory?.let { nav ->
                                { nav(title, "movie", "genre", "comedy") }
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // Swimlane: Film Horor
                if (uiState.horrorMovies.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.movies_horror)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.horrorMovies,
                            onSeeMoreClick = onNavigateToCategory?.let { nav ->
                                { nav(title, "movie", "genre", "horror") }
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // Swimlane: Film Fiksi Ilmiah (Sci-Fi)
                if (uiState.sciFiMovies.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.movies_scifi)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.sciFiMovies,
                            onSeeMoreClick = onNavigateToCategory?.let { nav ->
                                { nav(title, "movie", "genre", "science-fiction") }
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }
            }
        }
    }
}
