package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UnifiedMedia
import com.example.ui.components.*
import com.example.ui.viewmodel.CinemaViewModel
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    viewModel: CinemaViewModel,
    onMediaClick: (UnifiedMedia) -> Unit,
    modifier: Modifier = Modifier
) {
    val moviesState by viewModel.moviesState.collectAsStateWithLifecycle()
    val featuredList by viewModel.featuredSlider.collectAsStateWithLifecycle()
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
            viewModel.loadMovies(isRefresh = true)
            isRefreshing = false
        },
        state = pullToRefreshState,
        modifier = modifier.fillMaxSize().testTag("movies_screen_pull_refresh")
    ) {
        val gridState = rememberLazyGridState()

        // Infinite scroll detection
        val shouldLoadMore = remember {
            derivedStateOf {
                val totalItems = gridState.layoutInfo.totalItemsCount
                val lastVisibleIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                totalItems > 0 && lastVisibleIndex >= totalItems - 4
            }
        }

        LaunchedEffect(shouldLoadMore.value) {
            if (shouldLoadMore.value) {
                viewModel.loadMoreMovies()
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("movies_grid"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header 1: Featured Slider
            if (featuredList.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val pagerState = rememberPagerState(pageCount = { featuredList.size })

                    // Auto advance slider
                    LaunchedEffect(pagerState, featuredList.size) {
                        if (featuredList.size > 1) {
                            while (true) {
                                delay(6000)
                                if (!pagerState.isScrollInProgress) {
                                    val next = (pagerState.currentPage + 1) % featuredList.size
                                    pagerState.animateScrollToPage(next)
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("featured_horizontal_pager"),
                            pageSpacing = 12.dp
                        ) { page ->
                            val media = featuredList[page]
                            UnifiedHeroBanner(
                                media = media,
                                isWatchlisted = watchlistIds.contains(media.id),
                                onWatchClick = { onMediaClick(media) },
                                onDetailsClick = { onMediaClick(media) },
                                onToggleWatchlist = { viewModel.toggleWatchlist(it) }
                            )
                        }
                    }
                }
            }

            // Header 2: Genre & Sort Order Filters
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
            when (val state = moviesState) {
                is UiState.Loading -> {
                    items(6) {
                        SkeletonMediaCard()
                    }
                }
                is UiState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        ErrorRetryView(
                            message = state.message,
                            onRetry = { viewModel.loadMovies(isRefresh = true) }
                        )
                    }
                }
                is UiState.Empty -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyStateView(message = "فیلمی با این مشخصات یافت نشد.")
                    }
                }
                is UiState.Success -> {
                    items(state.data, key = { it.id }) { movie ->
                        UnifiedMediaCard(
                            media = movie,
                            isWatchlisted = watchlistIds.contains(movie.id),
                            onMediaClick = onMediaClick,
                            onToggleWatchlist = { viewModel.toggleWatchlist(it) }
                        )
                    }
                }
            }
        }
    }
}
