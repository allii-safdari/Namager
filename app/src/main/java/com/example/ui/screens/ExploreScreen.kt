package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.model.UnifiedMedia
import com.example.ui.viewmodel.CinemaViewModel

@Composable
fun ExploreScreen(
    viewModel: CinemaViewModel,
    watchlistIds: Set<Int> = emptySet(),
    onMediaClick: (UnifiedMedia) -> Unit,
    modifier: Modifier = Modifier
) {
    SearchScreen(
        viewModel = viewModel,
        onMediaClick = onMediaClick,
        modifier = modifier
    )
}
