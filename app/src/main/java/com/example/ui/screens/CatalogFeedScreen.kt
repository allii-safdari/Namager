package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.FilterOrder
import com.example.domain.model.Media
import com.example.domain.model.MediaKind
import com.example.ui.components.CinematicGlassTopBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorRetryView
import com.example.ui.components.GenreChipsRow
import com.example.ui.components.HeroBanner
import com.example.ui.components.HeroPagerIndicator
import com.example.ui.components.MediaCard
import com.example.ui.components.SkeletonMediaCard
import com.example.ui.components.fullBleed
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CatalogFeedUiState
import kotlinx.coroutines.delay

private const val HERO_AUTO_SCROLL_MS = 7_000L
private val GRID_HORIZONTAL_PADDING = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogFeedScreen(
    state: CatalogFeedUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onRetryLoadMore: () -> Unit,
    onGenreSelected: (Int?) -> Unit,
    onOrderSelected: (FilterOrder) -> Unit,
    onMediaClick: (Media) -> Unit,
    onToggleFavorite: (Media) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val gridState = rememberLazyGridState()
    val hasHero = state.featured.isNotEmpty()
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeight = statusBarTop + 56.dp
    val filtersIndex = if (hasHero) 1 else 0

    val shouldLoadMore by remember {
        derivedStateOf {
            val info = gridState.layoutInfo
            val totalItems = info.totalItemsCount
            val lastVisibleIndex = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onLoadMore()
    }

    val showPinnedFilters by remember(filtersIndex) {
        derivedStateOf { gridState.firstVisibleItemIndex > filtersIndex }
    }

    LaunchedEffect(state.selectedGenreId, state.order) {
        if (gridState.firstVisibleItemIndex > filtersIndex) {
            gridState.animateScrollToItem(filtersIndex)
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("catalog_feed_root")) {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                state = gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("catalog_feed_grid"),
                contentPadding = PaddingValues(
                    start = GRID_HORIZONTAL_PADDING,
                    end = GRID_HORIZONTAL_PADDING,
                    top = topBarHeight,
                    bottom = 104.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hasHero) {
                    item(
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = "hero"
                    ) {
                        val pagerState = rememberPagerState(pageCount = { state.featured.size })

                        LaunchedEffect(pagerState.settledPage, state.featured.size) {
                            if (state.featured.size > 1) {
                                delay(HERO_AUTO_SCROLL_MS)
                                if (!pagerState.isScrollInProgress) {
                                    val next = (pagerState.settledPage + 1) % state.featured.size
                                    pagerState.animateScrollToPage(next)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fullBleed(GRID_HORIZONTAL_PADDING)
                        ) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth(),
                                pageSpacing = 0.dp,
                                key = { page -> state.featured.getOrNull(page)?.id ?: page }
                            ) { page ->
                                val media = state.featured[page]
                                HeroBanner(
                                    media = media,
                                    isWatchlisted = state.favoriteIds.contains(media.id),
                                    onDetailsClick = onMediaClick,
                                    onToggleWatchlist = onToggleFavorite
                                )
                            }

                            if (state.featured.size > 1) {
                                HeroPagerIndicator(
                                    pageCount = state.featured.size,
                                    currentPage = pagerState.currentPage,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 18.dp)
                                )
                            }
                        }
                    }
                }

                item(
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = "filters"
                ) {
                    GenreChipsRow(
                        genres = state.genres,
                        selectedGenreId = state.selectedGenreId,
                        onGenreSelected = onGenreSelected,
                        modifier = Modifier
                            .fullBleed(GRID_HORIZONTAL_PADDING)
                            .padding(vertical = 4.dp)
                    )
                }

                when {
                    state.isInitialLoading -> {
                        items(count = 6, contentType = { "skeleton" }) {
                            SkeletonMediaCard()
                        }
                    }

                    state.error != null && state.items.isEmpty() -> {
                        item(span = { GridItemSpan(maxLineSpan) }, contentType = "error") {
                            ErrorRetryView(message = state.error, onRetry = onRetry)
                        }
                    }

                    state.items.isEmpty() && !state.isRefreshing -> {
                        item(span = { GridItemSpan(maxLineSpan) }, contentType = "empty") {
                            EmptyStateView(
                                message = if (state.kind == MediaKind.MOVIE) {
                                    "فیلمی با این مشخصات یافت نشد."
                                } else {
                                    "سریالی با این مشخصات یافت نشد."
                                }
                            )
                        }
                    }

                    else -> {
                        items(
                            items = state.items,
                            key = { it.id },
                            contentType = { "media" }
                        ) { media ->
                            MediaCard(
                                media = media,
                                isWatchlisted = state.favoriteIds.contains(media.id),
                                onMediaClick = onMediaClick,
                                onToggleWatchlist = onToggleFavorite
                            )
                        }

                        if (state.isLoadingMore) {
                            item(span = { GridItemSpan(maxLineSpan) }, contentType = "loading_more") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        } else if (state.loadMoreError != null) {
                            item(span = { GridItemSpan(maxLineSpan) }, contentType = "load_more_error") {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = state.loadMoreError,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    Button(
                                        onClick = onRetryLoadMore,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.Black
                                        )
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null
                                        )
                                        Text("تلاش دوباره", modifier = Modifier.padding(start = 6.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        CinematicGlassTopBar(
            title = if (state.kind == MediaKind.MOVIE) "فیلم‌ها" else "سریال‌ها",
            selectedOrder = state.order,
            onOrderSelected = onOrderSelected,
            onSearchClick = onSearchClick,
            modifier = Modifier.align(Alignment.TopCenter),
            testTag = "catalog_top_bar_${state.kind.name}"
        )

        AnimatedVisibility(
            visible = showPinnedFilters,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = topBarHeight)
        ) {
            Surface(
                color = Color(0xEE12121C),
                border = BorderStroke(0.5.dp, GlassBorder)
            ) {
                GenreChipsRow(
                    genres = state.genres,
                    selectedGenreId = state.selectedGenreId,
                    onGenreSelected = onGenreSelected,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 88.dp)
        )
    }
}
