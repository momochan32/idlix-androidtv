package com.idlix.tv.ui.screens.search

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.idlix.tv.R
import com.idlix.tv.ui.components.TvLoadingSpinner
import com.idlix.tv.ui.components.TvMovieCard
import com.idlix.tv.ui.theme.NetflixBlack
import com.idlix.tv.ui.theme.NetflixDarkCard
import com.idlix.tv.ui.theme.NetflixLightGray
import com.idlix.tv.ui.theme.NetflixRed
import com.idlix.tv.ui.theme.NetflixWhite
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToDetail: (contentType: String, slug: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val firstResultFocusRequester = remember { FocusRequester() }
    var isInputFocused by remember { mutableStateOf(false) }
    var pendingFocusOnResults by remember { mutableStateOf(false) }

    val quickSearches = listOf("Batman", "Spider-Man", "Action", "Korea", "Horror", "Comedy", "Anime", "Romance")

    val submitSearch = {
        keyboardController?.hide()
        viewModel.searchNow()
        pendingFocusOnResults = true
        if (uiState.results.isNotEmpty()) {
            try {
                firstResultFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(pendingFocusOnResults, uiState.isLoading, uiState.results) {
        if (pendingFocusOnResults && !uiState.isLoading) {
            if (uiState.results.isNotEmpty()) {
                delay(100)
                try {
                    firstResultFocusRequester.requestFocus()
                } catch (_: Exception) {}
            } else {
                focusManager.clearFocus()
            }
            pendingFocusOnResults = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NetflixBlack)
            .padding(horizontal = 28.dp, vertical = 20.dp)
    ) {
        // Search Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(NetflixDarkCard, RoundedCornerShape(8.dp))
                .border(
                    width = if (isInputFocused) 2.dp else 1.dp,
                    color = if (isInputFocused) NetflixWhite else NetflixDarkCard,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = if (isInputFocused) NetflixWhite else NetflixLightGray
            )

            Spacer(modifier = Modifier.width(10.dp))

            BasicTextField(
                value = uiState.query,
                onValueChange = { viewModel.onQueryChange(it) },
                textStyle = TextStyle(
                    color = NetflixWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(NetflixRed),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        submitSearch()
                    }
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { isInputFocused = it.isFocused }
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                            when (keyEvent.nativeKeyEvent.keyCode) {
                                KeyEvent.KEYCODE_DPAD_DOWN -> {
                                    keyboardController?.hide()
                                    if (uiState.results.isNotEmpty()) {
                                        try {
                                            firstResultFocusRequester.requestFocus()
                                            true
                                        } catch (_: Exception) {
                                            focusManager.moveFocus(FocusDirection.Down)
                                        }
                                    } else {
                                        focusManager.moveFocus(FocusDirection.Down)
                                    }
                                }
                                KeyEvent.KEYCODE_ENTER,
                                KeyEvent.KEYCODE_NUMPAD_ENTER,
                                KeyEvent.KEYCODE_SEARCH -> {
                                    submitSearch()
                                    true
                                }
                                KeyEvent.KEYCODE_BACK -> {
                                    if (isInputFocused) {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        true
                                    } else {
                                        false
                                    }
                                }
                                else -> false
                            }
                        } else {
                            false
                        }
                    },
                decorationBox = { innerTextField ->
                    if (uiState.query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_placeholder),
                            color = NetflixLightGray,
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            )

            if (uiState.query.isNotEmpty()) {
                Button(
                    onClick = {
                        viewModel.onQueryChange("")
                        pendingFocusOnResults = false
                    },
                    colors = ButtonDefaults.colors(
                        containerColor = NetflixBlack,
                        contentColor = NetflixLightGray,
                        focusedContainerColor = NetflixRed,
                        focusedContentColor = NetflixWhite
                    ),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(6.dp))
                ) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Search Tags for TV Remote Convenience
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.popular_searches),
                color = NetflixLightGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            TvLazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickSearches) { tag ->
                    Button(
                        onClick = {
                            viewModel.onQueryChange(tag)
                            pendingFocusOnResults = true
                        },
                        colors = ButtonDefaults.colors(
                            containerColor = NetflixDarkCard,
                            contentColor = NetflixLightGray,
                            focusedContainerColor = NetflixRed,
                            focusedContentColor = NetflixWhite
                        ),
                        shape = ButtonDefaults.shape(shape = RoundedCornerShape(4.dp))
                    ) {
                        Text(text = tag, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Results Section
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                TvLoadingSpinner(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.results.isEmpty() && uiState.query.isNotBlank()) {
                Text(
                    text = stringResource(R.string.no_results_for, uiState.query),
                    color = NetflixLightGray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.results.isEmpty()) {
                Text(
                    text = stringResource(R.string.search_hint),
                    color = NetflixLightGray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                TvLazyVerticalGrid(
                    columns = TvGridCells.Adaptive(135.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        count = uiState.results.size,
                        key = { index -> uiState.results[index].id }
                    ) { index ->
                        val item = uiState.results[index]
                        TvMovieCard(
                            title = item.title,
                            posterUrl = item.fullPosterUrl,
                            rating = item.formattedRating,
                            quality = item.quality ?: if (item.contentType == "tv_series") "SERIES" else "MOVIE",
                            modifier = if (index == 0) Modifier.focusRequester(firstResultFocusRequester) else Modifier,
                            onClick = { onNavigateToDetail(item.contentType, item.slug) }
                        )
                    }
                }
            }
        }
    }
}
