package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UnifiedMedia
import com.example.ui.components.*
import com.example.ui.viewmodel.CinemaViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesScreen(
    viewModel: CinemaViewModel,
    onMediaClick: (UnifiedMedia) -> Unit,
    modifier: Modifier = Modifier
) {
    val seriesState by viewModel.seriesState.collectAsStateWithLifecycle()
    val genres by viewModel.genres.collectAsStateWithLifecycle()
    val selectedGenreId by viewModel.selectedGenreId.collectAsStateWithLifecycle()
    val selectedOrder by viewModel.selectedOrder.collectAsStateWithLifecycle()
    val watchlistIds by viewModel.watchlistIds.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.loadSeries(isRefresh = true)
            isRefreshing = false
        },
        state = pullToRefreshState,
        modifier = modifier.fillMaxSize().testTag("series_screen_pull_refresh")
    ) {
        val gridState = rememberLazyGridState()

        val shouldLoadMore = remember {
            derivedStateOf {
                val totalItems = gridState.layoutInfo.totalItemsCount
                val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                totalItems > 0 && lastVisibleIndex >= totalItems - 4
            }
        }

        LaunchedEffect(shouldLoadMore.value) {
            if (shouldLoadMore.value) {
                viewModel.loadMoreSeries()
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("series_grid"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Genre and sort order filter chips
            item(span = { GridItemSpan(maxLineSpan) }) {
                GenreFilterSection(
                    genres = genres,
                    selectedGenreId = selectedGenreId,
                    selectedOrder = selectedOrder,
                    onGenreSelected = { viewModel.setGenreFilter(it) },
                    onOrderSelected = { viewModel.setSortOrder(it) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // State Handling: Skeleton, Error, Empty, Success
            when (val state = seriesState) {
                is UiState.Loading -> {
                    items(6) {
                        SkeletonMediaCard()
                    }
                }
                is UiState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ErrorRetryView(
                            message = state.message,
                            onRetry = { viewModel.loadSeries(isRefresh = true) }
                        )
                    }
                }
                is UiState.Empty -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyStateView(message = "سریالی با این مشخصات یافت نشد.")
                    }
                }
                is UiState.Success -> {
                    items(state.data, key = { it.id }) { series ->
                        UnifiedMediaCard(
                            media = series,
                            isWatchlisted = watchlistIds.contains(series.id),
                            onMediaClick = onMediaClick,
                            onToggleWatchlist = { viewModel.toggleWatchlist(it) }
                        )
                    }
                }
            }
        }
    }
}
