package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ScreenTab
import com.example.ui.components.CinemaTopBar
import com.example.ui.player.ExoPlayerView
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.util.PersianUtils
import com.example.ui.viewmodel.CinemaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: CinemaViewModel = viewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            CinematicTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    CinematicApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CinematicApp(
    viewModel: CinemaViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedMedia by viewModel.selectedMedia.collectAsStateWithLifecycle()
    val favoriteItems by viewModel.favoriteItems.collectAsStateWithLifecycle()
    val activePlayingSource by viewModel.activePlayingSource.collectAsStateWithLifecycle()
    val activePlayingTitle by viewModel.activePlayingTitle.collectAsStateWithLifecycle()
    val activePlayingSubtitle by viewModel.activePlayingSubtitle.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    // Fullscreen Media3 Player if playing
    if (activePlayingSource != null) {
        ExoPlayerView(
            source = activePlayingSource!!,
            title = activePlayingTitle,
            subtitle = activePlayingSubtitle,
            seekJumpSeconds = settings.seekJumpSeconds,
            onClose = { viewModel.closePlayer() },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    // Detail Screen overlay if a movie/series is selected
    if (selectedMedia != null) {
        MovieDetailSheet(
            media = selectedMedia!!,
            viewModel = viewModel,
            isWatchlisted = viewModel.isFavorite(selectedMedia!!.id),
            onClose = { viewModel.selectMedia(null) }
        )
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp

        if (isWideScreen) {
            // Tablet & Android TV layout: NavigationRail on side + content
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
            ) {
                CinemaNavigationRail(
                    currentTab = currentTab,
                    watchlistCount = favoriteItems.size,
                    onTabSelected = { viewModel.setTab(it) }
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    CinemaTopBar(
                        onSearchClick = { viewModel.setTab(ScreenTab.SEARCH) }
                    )

                    AnimatedTabContent(
                        currentTab = currentTab,
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // Handheld layout: TopBar + Screen Content + NavigationBar at bottom
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("cinematic_main_scaffold"),
                containerColor = DarkBackground,
                topBar = {
                    CinemaTopBar(
                        onSearchClick = { viewModel.setTab(ScreenTab.SEARCH) }
                    )
                },
                bottomBar = {
                    CinemaBottomNavigationBar(
                        currentTab = currentTab,
                        watchlistCount = favoriteItems.size,
                        onTabSelected = { viewModel.setTab(it) }
                    )
                }
            ) { innerPadding ->
                AnimatedTabContent(
                    currentTab = currentTab,
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun AnimatedTabContent(
    currentTab: ScreenTab,
    viewModel: CinemaViewModel,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "TabContentAnimation"
        ) { tab ->
            when (tab) {
                ScreenTab.MOVIES -> {
                    MoviesScreen(
                        viewModel = viewModel,
                        onMediaClick = { viewModel.selectMedia(it) }
                    )
                }
                ScreenTab.SERIES -> {
                    SeriesScreen(
                        viewModel = viewModel,
                        onMediaClick = { viewModel.selectMedia(it) }
                    )
                }
                ScreenTab.SEARCH -> {
                    SearchScreen(
                        viewModel = viewModel,
                        onMediaClick = { viewModel.selectMedia(it) }
                    )
                }
                ScreenTab.FAVORITES -> {
                    FavoritesScreen(
                        viewModel = viewModel,
                        onMediaClick = { viewModel.selectMedia(it) }
                    )
                }
                ScreenTab.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun CinemaBottomNavigationBar(
    currentTab: ScreenTab,
    watchlistCount: Int,
    onTabSelected: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .testTag("cinema_bottom_nav_bar")
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = DarkSurface,
        tonalElevation = 8.dp
    ) {
        // Tab 1: Movies (فیلم‌ها)
        NavigationBarItem(
            selected = currentTab == ScreenTab.MOVIES,
            onClick = { onTabSelected(ScreenTab.MOVIES) },
            icon = {
                Icon(
                    imageVector = if (currentTab == ScreenTab.MOVIES) Icons.Default.Movie else Icons.Outlined.Movie,
                    contentDescription = stringResource(id = R.string.nav_movies)
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.nav_movies),
                    fontWeight = if (currentTab == ScreenTab.MOVIES) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CinemaGold,
                indicatorColor = CinemaGold,
                unselectedIconColor = TextTertiary,
                unselectedTextColor = TextTertiary
            ),
            modifier = Modifier.testTag("nav_item_movies")
        )

        // Tab 2: Series (سریال‌ها)
        NavigationBarItem(
            selected = currentTab == ScreenTab.SERIES,
            onClick = { onTabSelected(ScreenTab.SERIES) },
            icon = {
                Icon(
                    imageVector = if (currentTab == ScreenTab.SERIES) Icons.Default.Tv else Icons.Outlined.Tv,
                    contentDescription = stringResource(id = R.string.nav_series)
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.nav_series),
                    fontWeight = if (currentTab == ScreenTab.SERIES) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CinemaGold,
                indicatorColor = CinemaGold,
                unselectedIconColor = TextTertiary,
                unselectedTextColor = TextTertiary
            ),
            modifier = Modifier.testTag("nav_item_series")
        )

        // Tab 3: Search (جستجو)
        NavigationBarItem(
            selected = currentTab == ScreenTab.SEARCH,
            onClick = { onTabSelected(ScreenTab.SEARCH) },
            icon = {
                Icon(
                    imageVector = if (currentTab == ScreenTab.SEARCH) Icons.Default.Search else Icons.Outlined.Search,
                    contentDescription = stringResource(id = R.string.nav_search)
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.nav_search),
                    fontWeight = if (currentTab == ScreenTab.SEARCH) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CinemaGold,
                indicatorColor = CinemaGold,
                unselectedIconColor = TextTertiary,
                unselectedTextColor = TextTertiary
            ),
            modifier = Modifier.testTag("nav_item_search")
        )

        // Tab 4: Favorites (علاقه‌مندی‌ها)
        NavigationBarItem(
            selected = currentTab == ScreenTab.FAVORITES,
            onClick = { onTabSelected(ScreenTab.FAVORITES) },
            icon = {
                BadgedBox(
                    badge = {
                        if (watchlistCount > 0) {
                            Badge(
                                containerColor = CinemaRed,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = PersianUtils.toPersianDigits(watchlistCount),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (currentTab == ScreenTab.FAVORITES) Icons.Default.Bookmarks else Icons.Outlined.Bookmarks,
                        contentDescription = stringResource(id = R.string.nav_favorites)
                    )
                }
            },
            label = {
                Text(
                    text = stringResource(id = R.string.nav_favorites),
                    fontWeight = if (currentTab == ScreenTab.FAVORITES) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CinemaGold,
                indicatorColor = CinemaGold,
                unselectedIconColor = TextTertiary,
                unselectedTextColor = TextTertiary
            ),
            modifier = Modifier.testTag("nav_item_favorites")
        )

        // Tab 5: Settings (تنظیمات)
        NavigationBarItem(
            selected = currentTab == ScreenTab.SETTINGS,
            onClick = { onTabSelected(ScreenTab.SETTINGS) },
            icon = {
                Icon(
                    imageVector = if (currentTab == ScreenTab.SETTINGS) Icons.Default.Settings else Icons.Outlined.Settings,
                    contentDescription = stringResource(id = R.string.nav_settings)
                )
            },
            label = {
                Text(
                    text = stringResource(id = R.string.nav_settings),
                    fontWeight = if (currentTab == ScreenTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CinemaGold,
                indicatorColor = CinemaGold,
                unselectedIconColor = TextTertiary,
                unselectedTextColor = TextTertiary
            ),
            modifier = Modifier.testTag("nav_item_settings")
        )
    }
}

@Composable
fun CinemaNavigationRail(
    currentTab: ScreenTab,
    watchlistCount: Int,
    onTabSelected: (ScreenTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .testTag("cinema_navigation_rail"),
        containerColor = DarkSurface
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        NavigationRailItem(
            selected = currentTab == ScreenTab.MOVIES,
            onClick = { onTabSelected(ScreenTab.MOVIES) },
            icon = { Icon(Icons.Default.Movie, contentDescription = null) },
            label = { Text("فیلم‌ها") },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = CinemaGold
            )
        )

        NavigationRailItem(
            selected = currentTab == ScreenTab.SERIES,
            onClick = { onTabSelected(ScreenTab.SERIES) },
            icon = { Icon(Icons.Default.Tv, contentDescription = null) },
            label = { Text("سریال‌ها") },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = CinemaGold
            )
        )

        NavigationRailItem(
            selected = currentTab == ScreenTab.SEARCH,
            onClick = { onTabSelected(ScreenTab.SEARCH) },
            icon = { Icon(Icons.Default.Search, contentDescription = null) },
            label = { Text("جستجو") },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = CinemaGold
            )
        )

        NavigationRailItem(
            selected = currentTab == ScreenTab.FAVORITES,
            onClick = { onTabSelected(ScreenTab.FAVORITES) },
            icon = {
                BadgedBox(
                    badge = {
                        if (watchlistCount > 0) {
                            Badge(containerColor = CinemaRed) {
                                Text(PersianUtils.toPersianDigits(watchlistCount))
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Bookmarks, contentDescription = null)
                }
            },
            label = { Text("علاقه‌مندی‌ها") },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = CinemaGold
            )
        )

        NavigationRailItem(
            selected = currentTab == ScreenTab.SETTINGS,
            onClick = { onTabSelected(ScreenTab.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("تنظیمات") },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = CinemaGold
            )
        )
    }
}
