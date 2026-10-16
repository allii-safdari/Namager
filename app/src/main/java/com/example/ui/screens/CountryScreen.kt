package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Country
import com.example.data.model.FilterOrder
import com.example.data.model.UnifiedMedia
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorRetryView
import com.example.ui.components.SkeletonMediaCard
import com.example.ui.components.UnifiedMediaCard
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.CinemaViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryScreen(
    country: Country,
    viewModel: CinemaViewModel,
    onBack: () -> Unit,
    onMediaClick: (UnifiedMedia) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val contentState by viewModel.countryContentState.collectAsStateWithLifecycle()
    val selectedOrder by viewModel.selectedOrder.collectAsStateWithLifecycle()
    val watchlistIds by viewModel.watchlistIds.collectAsStateWithLifecycle()

    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "آثار کشور ${country.title}",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground,
        modifier = modifier.fillMaxSize().testTag("country_screen")
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.loadCountryContent(country, isRefresh = true)
                isRefreshing = false
            },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                    viewModel.loadMoreCountryContent()
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                state = gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("country_content_grid"),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sort Order Bar
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مرتب‌سازی:",
                            style = MaterialTheme.typography.labelMedium,
                            color = CinemaGold,
                            fontWeight = FontWeight.Bold
                        )

                        FilterOrder.entries.forEach { order ->
                            val isSelected = (selectedOrder == order)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSortOrder(order) },
                                label = { Text(order.titleFa) },
                                shape = CircleShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CinemaGold,
                                    selectedLabelColor = Color.Black,
                                    containerColor = DarkSurface,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }
                }

                // States
                when (val state = contentState) {
                    is UiState.Loading -> {
                        items(6) {
                            SkeletonMediaCard()
                        }
                    }
                    is UiState.Error -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ErrorRetryView(
                                message = state.message,
                                onRetry = { viewModel.loadCountryContent(country, isRefresh = true) }
                            )
                        }
                    }
                    is UiState.Empty -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyStateView(message = "اثری برای کشور ${country.title} ثبت نشده است.")
                        }
                    }
                    is UiState.Success -> {
                        items(state.data, key = { it.id }) { item ->
                            UnifiedMediaCard(
                                media = item,
                                isWatchlisted = watchlistIds.contains(item.id),
                                onMediaClick = onMediaClick,
                                onToggleWatchlist = { viewModel.toggleWatchlist(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}
