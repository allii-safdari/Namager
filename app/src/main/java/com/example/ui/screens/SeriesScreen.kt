package com.example.ui.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.Media
import com.example.ui.viewmodel.SeriesViewModel

@Composable
fun SeriesScreen(
    onMediaClick: (Media) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SeriesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message -> snackbarHostState.showSnackbar(message) }
    }

    CatalogFeedScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onRefresh = viewModel::refresh,
        onLoadMore = viewModel::loadMore,
        onRetry = viewModel::retry,
        onRetryLoadMore = viewModel::retryLoadMore,
        onGenreSelected = viewModel::setGenre,
        onOrderSelected = viewModel::setOrder,
        onMediaClick = onMediaClick,
        onToggleFavorite = viewModel::toggleFavorite,
        onSearchClick = onSearchClick,
        modifier = modifier
    )
}
