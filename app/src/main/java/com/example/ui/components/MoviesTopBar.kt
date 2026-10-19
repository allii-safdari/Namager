package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.FilterOrder
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesTopBar(
    selectedOrder: FilterOrder,
    onOrderSelected: (FilterOrder) -> Unit,
    onSearchClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier
) {
    var menuOpen by remember { mutableStateOf(false) }

    Column(modifier = modifier.testTag("movies_top_bar")) {
        TopAppBar(
            title = {
                Text(
                    text = "فیلم‌ها",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            actions = {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.testTag("top_bar_search")
                ) {
                    Icon(Icons.Default.Search, contentDescription = "جستجو", tint = TextPrimary)
                }
                Box {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier.testTag("top_bar_sort")
                    ) {
                        Icon(Icons.Default.Sort, contentDescription = "مرتب‌سازی", tint = TextPrimary)
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false }
                    ) {
                        FilterOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = { Text(order.titleFa) },
                                leadingIcon = {
                                    if (order == selectedOrder) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = CinemaGold
                                        )
                                    }
                                },
                                onClick = {
                                    onOrderSelected(order)
                                    menuOpen = false
                                },
                                modifier = Modifier.testTag("sort_item_${order.value}")
                            )
                        }
                    }
                }
            },
            windowInsets = WindowInsets(0, 0, 0, 0),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = DarkBackground,
                scrolledContainerColor = DarkBackground,
                titleContentColor = TextPrimary,
                actionIconContentColor = TextPrimary
            ),
            scrollBehavior = scrollBehavior
        )
        HorizontalDivider(thickness = 0.5.dp, color = GlassBorder)
    }
}