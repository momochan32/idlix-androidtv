package com.idlix.tv.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Text
import com.idlix.tv.R
import com.idlix.tv.ui.components.TvContinueWatchingRow
import com.idlix.tv.ui.components.TvHeroBillboard
import com.idlix.tv.ui.components.TvLoadingSpinner
import com.idlix.tv.ui.components.TvSwimlaneRow
import com.idlix.tv.ui.theme.NetflixBlack
import com.idlix.tv.ui.theme.NetflixWhite

@Composable
fun HomeScreen(
    onNavigateToDetail: (contentType: String, slug: String) -> Unit,
    onNavigateToPlayer: (contentType: String, contentId: String, title: String, poster: String?, backdrop: String?, slug: String?) -> Unit,
    onNavigateToCategory: (title: String, contentType: String, filterType: String, filterValue: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val heroFocusRequester = remember { FocusRequester() }

    // Refresh continue watching list on screen entrance / return
    LaunchedEffect(Unit) {
        viewModel.refreshContinueWatching()
    }

    // Direct initial focus to Hero billboard content as soon as loaded
    LaunchedEffect(uiState.isLoading, uiState.heroMovie) {
        if (!uiState.isLoading && uiState.heroMovie != null) {
            try {
                heroFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
    ) {
        if (uiState.isLoading) {
            TvLoadingSpinner(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (uiState.error != null && uiState.heroMovie == null) {
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
                // 1. Dynamic Hero Billboard
                item {
                    TvHeroBillboard(
                        movie = uiState.heroMovie,
                        playButtonModifier = Modifier.focusRequester(heroFocusRequester),
                        onPlayClick = {
                            uiState.heroMovie?.let {
                                if (it.contentType == "tv_series") {
                                    onNavigateToDetail(it.contentType, it.slug)
                                } else {
                                    onNavigateToPlayer(
                                        it.contentType,
                                        it.id,
                                        it.title,
                                        it.fullPosterUrl,
                                        it.fullBackdropUrl,
                                        it.slug
                                    )
                                }
                            }
                        },
                        onDetailsClick = {
                            uiState.heroMovie?.let {
                                onNavigateToDetail(it.contentType, it.slug)
                            }
                        }
                    )
                }

                // 2. Bar Paling Pertama: Lanjutkan Menonton
                if (uiState.continueWatching.isNotEmpty()) {
                    item {
                        TvContinueWatchingRow(
                            title = stringResource(R.string.continue_watching),
                            items = uiState.continueWatching,
                            onItemClick = { item ->
                                onNavigateToPlayer(
                                    item.contentType,
                                    item.contentId,
                                    item.title,
                                    item.posterUrl,
                                    item.backdropUrl,
                                    item.slug
                                )
                            }
                        )
                    }
                }

                // 3. Swimlane: Sedang Tren Sekarang
                if (uiState.trending.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.trending_now)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.trending,
                            onSeeMoreClick = {
                                onNavigateToCategory(title, "movie", "sort", "popularity")
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // 4. Swimlane: Film Terpopuler
                if (uiState.popularMovies.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.top_10_movies)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.popularMovies,
                            onSeeMoreClick = {
                                onNavigateToCategory(title, "movie", "sort", "")
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // 5. Swimlane: Film Baru Rilis
                if (uiState.latestMovies.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.latest_movies)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.latestMovies,
                            onSeeMoreClick = {
                                onNavigateToCategory(title, "movie", "sort", "createdAt")
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // 6. Swimlane: Serial TV Pilihan
                if (uiState.popularSeries.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.popular_series)
                        val seriesAsMovies = remember(uiState.popularSeries) {
                            uiState.popularSeries.map { it.toMovieItem() }
                        }
                        TvSwimlaneRow(
                            title = title,
                            items = seriesAsMovies,
                            onSeeMoreClick = {
                                onNavigateToCategory(title, "tv_series", "sort", "popularity")
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // 7. Swimlane: Serial TV Baru Rilis
                if (uiState.latestSeries.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.latest_series)
                        val latestSeriesAsMovies = remember(uiState.latestSeries) {
                            uiState.latestSeries.map { it.toMovieItem() }
                        }
                        TvSwimlaneRow(
                            title = title,
                            items = latestSeriesAsMovies,
                            onSeeMoreClick = {
                                onNavigateToCategory(title, "tv_series", "sort", "createdAt")
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // 8. Swimlane: Film Aksi & Petualangan
                if (uiState.actionMovies.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.movies_action)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.actionMovies,
                            onSeeMoreClick = {
                                onNavigateToCategory(title, "movie", "genre", "action")
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // 9. Swimlane: Film Horor & Misteri
                if (uiState.horrorMovies.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.movies_horror)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.horrorMovies,
                            onSeeMoreClick = {
                                onNavigateToCategory(title, "movie", "genre", "horror")
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // 10. Swimlane: Film Komedi Seru
                if (uiState.comedyMovies.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.movies_comedy)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.comedyMovies,
                            onSeeMoreClick = {
                                onNavigateToCategory(title, "movie", "genre", "comedy")
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // 11. Swimlane: Film Animasi & Anime
                if (uiState.animationMovies.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.movies_animation)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.animationMovies,
                            onSeeMoreClick = {
                                onNavigateToCategory(title, "movie", "genre", "animation")
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // 12. Swimlane: Film Fiksi Ilmiah & Fantasi
                if (uiState.sciFiMovies.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.movies_scifi)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.sciFiMovies,
                            onSeeMoreClick = {
                                onNavigateToCategory(title, "movie", "genre", "science-fiction")
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }

                // 13. Swimlane: Film Romantis & Drama
                if (uiState.romanceMovies.isNotEmpty()) {
                    item {
                        val title = stringResource(R.string.movies_romance)
                        TvSwimlaneRow(
                            title = title,
                            items = uiState.romanceMovies,
                            onSeeMoreClick = {
                                onNavigateToCategory(title, "movie", "genre", "romance")
                            },
                            onItemClick = { onNavigateToDetail(it.contentType, it.slug) }
                        )
                    }
                }
            }
        }
    }
}
