package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.model.UnifiedMedia
import com.example.ui.viewmodel.CinemaViewModel

@Composable
fun WatchlistScreen(
    viewModel: CinemaViewModel,
    watchlistMedia: List<UnifiedMedia> = emptyList(),
    onMediaClick: (UnifiedMedia) -> Unit,
    onWatchClick: (UnifiedMedia) -> Unit = onMediaClick,
    modifier: Modifier = Modifier
) {
    FavoritesScreen(
        viewModel = viewModel,
        onMediaClick = onMediaClick,
        modifier = modifier
    )
}
