package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UnifiedMedia
import com.example.ui.components.*
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.viewmodel.CinemaViewModel
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.delay

private const val SLIDER_AUTO_SCROLL_MS = 6000L
private val GRID_HORIZONTAL_PADDING = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesScreen(
    viewModel: CinemaViewModel,
    onMediaClick: (UnifiedMedia) -> Unit,
    onSearchClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val seriesState by viewModel.seriesState.collectAsStateWithLifecycle()
    val featuredList by viewModel.featuredSeriesSlider.collectAsStateWithLifecycle()
    val genres by viewModel.genres.collectAsStateWithLifecycle()
    val selectedGenreId by viewModel.selectedGenreId.collectAsStateWithLifecycle()
    val selectedOrder by viewModel.selectedOrder.collectAsStateWithLifecycle()
    val watchlistIds by viewModel.watchlistIds.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isSeriesRefreshing.collectAsStateWithLifecycle()

    val pullToRefreshState = rememberPullToRefreshState()
    val hasSlider = featuredList.isNotEmpty()
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeight = statusBarTop + 64.dp

    // ایندکس چیپ‌های فیلتر: بعد از اسلایدر (در صورت وجود)
    val filtersIndex = if (hasSlider) 1 else 0

    val gridState = rememberLazyGridState()

    // Infinite scroll: لود ادامه‌ی لیست سریال‌ها هنگام رسیدن به انتهای صفحه
    val shouldLoadMore by remember {
        derivedStateOf {
            val info = gridState.layoutInfo
            val totalItems = info.totalItemsCount
            val lastVisibleIndex = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            seriesState is UiState.Success &&
                    totalItems > 0 &&
                    lastVisibleIndex >= totalItems - 4
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMoreSeries()
    }

    // نمایش فیلترهای چسبان (Pinned) هنگام اسکرول به پایین
    val showPinnedGenres by remember(filtersIndex) {
        derivedStateOf { gridState.firstVisibleItemIndex > filtersIndex }
    }

    // اسکرول نرم به ابتدای لیست در صورت تغییر فیلتر یا ترتیب
    LaunchedEffect(selectedGenreId, selectedOrder) {
        if (gridState.firstVisibleItemIndex > filtersIndex) {
            gridState.animateScrollToItem(filtersIndex)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("series_screen_root")
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshSeries() },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .testTag("series_screen_pull_refresh")
        ) {

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                state = gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("series_grid"),
                contentPadding = PaddingValues(
                    start = GRID_HORIZONTAL_PADDING,
                    end = GRID_HORIZONTAL_PADDING,
                    top = if (hasSlider) 0.dp else topBarHeight,
                    bottom = 96.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // اسلایدشو سریال‌های شاخص (از زیر نوار شیشه‌ای شروع می‌شود)
                if (hasSlider) {
                    item(key = "series_featured_slider", span = { GridItemSpan(maxLineSpan) }) {
                        val pagerState = rememberPagerState(pageCount = { featuredList.size })

                        LaunchedEffect(pagerState.settledPage, featuredList.size) {
                            if (featuredList.size > 1) {
                                delay(SLIDER_AUTO_SCROLL_MS)
                                if (!pagerState.isScrollInProgress) {
                                    val next = (pagerState.settledPage + 1) % featuredList.size
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("series_featured_horizontal_pager"),
                                pageSpacing = 0.dp,
                                key = { page -> featuredList.getOrNull(page)?.id ?: page }
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

                            if (featuredList.size > 1) {
                                HeroPagerIndicator(
                                    pageCount = featuredList.size,
                                    currentPage = pagerState.currentPage,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 24.dp)
                                        .testTag("series_featured_pager_dots")
                                )
                            }
                        }
                    }
                }

                // چیپ‌های فیلتر ژانر داخل گرید با استایل شیشه‌ای
                item(key = "series_genre_filters", span = { GridItemSpan(maxLineSpan) }) {
                    GenreChipsRow(
                        genres = genres,
                        selectedGenreId = selectedGenreId,
                        onGenreSelected = { viewModel.setGenreFilter(it) },
                        modifier = Modifier
                            .fullBleed(GRID_HORIZONTAL_PADDING)
                            .padding(vertical = 8.dp)
                    )
                }

                // مدیریت وضعیت‌ها: اسکلتون، خطا، خالی، موفق
                when (val state = seriesState) {
                    is UiState.Loading -> {
                        items(6) { SkeletonMediaCard() }
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

        // نوار بالایی شیشه‌ای (Glassmorphism TopAppBar) روی اسلایدشو
        CinematicGlassTopBar(
            title = "سریال‌ها",
            selectedOrder = selectedOrder,
            onOrderSelected = { viewModel.setSortOrder(it) },
            onSearchClick = onSearchClick,
            modifier = Modifier.align(Alignment.TopCenter),
            testTag = "series_top_bar"
        )

        // چیپ‌های فیلتر پین‌شده زیر نوار بالایی هنگام اسکرول
        AnimatedVisibility(
            visible = showPinnedGenres,
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
                    genres = genres,
                    selectedGenreId = selectedGenreId,
                    onGenreSelected = { viewModel.setGenreFilter(it) },
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}
