package com.example

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.AppNavHost
import com.example.ui.navigation.AppRoutes
import com.example.ui.navigation.navigateTopLevel
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.NamagerTheme
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextTertiary
import com.example.ui.util.PersianUtils
import com.example.ui.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.ui.NamagerSplashScreen
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
   val splash=    installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val favoriteCount by appViewModel.favoriteCount.collectAsStateWithLifecycle()


            splash.setOnExitAnimationListener { provider ->
                provider.iconView.animate().scaleX(1.15f).scaleY(1.15f).setDuration(250).start()
                provider.view.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction { provider.remove() }
                    .start()
            }


            NamagerTheme(
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

                    CinematicApp(favoriteCount = favoriteCount)
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CinematicApp(
    favoriteCount: Int,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showNavigation = currentRoute == null || currentRoute in AppRoutes.topLevelRoutes

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        val useRail = maxWidth >= 720.dp && showNavigation

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail) {
                CinemaNavigationRail(
                    currentRoute = currentRoute,
                    favoriteCount = favoriteCount,
                    onSelect = { navController.navigateTopLevel(it) }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                AppNavHost(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )

                if (showNavigation && !useRail) {
                    CinemaBottomNavigationBar(
                        currentRoute = currentRoute,
                        favoriteCount = favoriteCount,
                        onSelect = { navController.navigateTopLevel(it) },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

private data class TopLevelDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val destinations = listOf(
    TopLevelDestination(
        route = AppRoutes.MOVIES,
        labelRes = R.string.nav_movies,
        selectedIcon = Icons.Filled.Movie,
        unselectedIcon = Icons.Outlined.Movie
    ),
    TopLevelDestination(
        route = AppRoutes.SERIES,
        labelRes = R.string.nav_series,
        selectedIcon = Icons.Filled.Tv,
        unselectedIcon = Icons.Outlined.Tv
    ),
    TopLevelDestination(
        route = AppRoutes.SEARCH,
        labelRes = R.string.nav_search,
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    ),
    TopLevelDestination(
        route = AppRoutes.FAVORITES,
        labelRes = R.string.nav_favorites,
        selectedIcon = Icons.Filled.Bookmarks,
        unselectedIcon = Icons.Outlined.Bookmarks
    ),
    TopLevelDestination(
        route = AppRoutes.SETTINGS,
        labelRes = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)

@Composable
fun CinemaBottomNavigationBar(
    currentRoute: String?,
    favoriteCount: Int,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("cinema_bottom_nav_bar"),
       // containerColor = DarkSurface,
        tonalElevation = 8.dp
    ) {
        destinations.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(destination.route) },
                icon = {
                    if (destination.route == AppRoutes.FAVORITES && favoriteCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = CinemaGold, contentColor = Color.Black) {
                                    Text(
                                        text = PersianUtils.toPersianDigits(favoriteCount),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selected) {
                                    destination.selectedIcon
                                } else {
                                    destination.unselectedIcon
                                },
                                contentDescription = stringResource(destination.labelRes)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (selected) {
                                destination.selectedIcon
                            } else {
                                destination.unselectedIcon
                            },
                            contentDescription = stringResource(destination.labelRes)
                        )
                    }
                },
                label = {
                    Text(
                        text = stringResource(destination.labelRes),
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = CinemaGold,
                    indicatorColor = CinemaGold,
                    unselectedIconColor = TextTertiary,
                    unselectedTextColor = TextTertiary
                ),
                modifier = Modifier.testTag("nav_item_${destination.route}")
            )
        }
    }
}

@Composable
fun CinemaNavigationRail(
    currentRoute: String?,
    favoriteCount: Int,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("cinema_navigation_rail"),
        containerColor = DarkSurface
    ) {
        destinations.forEach { destination ->
            val selected = currentRoute == destination.route
            NavigationRailItem(
                selected = selected,
                onClick = { onSelect(destination.route) },
                icon = {
                    if (destination.route == AppRoutes.FAVORITES && favoriteCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = CinemaGold, contentColor = Color.Black) {
                                    Text(
                                        text = PersianUtils.toPersianDigits(favoriteCount),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selected) {
                                    destination.selectedIcon
                                } else {
                                    destination.unselectedIcon
                                },
                                contentDescription = null
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (selected) {
                                destination.selectedIcon
                            } else {
                                destination.unselectedIcon
                            },
                            contentDescription = null
                        )
                    }
                },
                label = { Text(stringResource(destination.labelRes)) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = CinemaGold,
                    indicatorColor = CinemaGold,
                    unselectedIconColor = TextTertiary,
                    unselectedTextColor = TextTertiary
                ),
                modifier = Modifier.testTag("rail_item_${destination.route}")
            )
        }
    }
}
