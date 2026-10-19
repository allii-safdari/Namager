package com.example.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.model.FilterOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesTopBar(
    selectedOrder: FilterOrder,
    onOrderSelected: (FilterOrder) -> Unit,
    onSearchClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier
) {
    CinematicGlassTopBar(
        title = "فیلم‌ها",
        selectedOrder = selectedOrder,
        onOrderSelected = onOrderSelected,
        onSearchClick = onSearchClick,
        modifier = modifier,
        testTag = "movies_top_bar"
    )
}