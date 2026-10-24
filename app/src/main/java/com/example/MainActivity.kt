package com.example

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.FlameLight
import com.example.ui.theme.NamagerTheme
import com.example.ui.theme.TextTertiary
import com.example.ui.util.PersianUtils
import com.example.ui.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.ui.navigation.destinations
import com.example.ui.theme.Flame

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = hiltViewModel()
            val favoriteCount by appViewModel.favoriteCount.collectAsStateWithLifecycle()

            NamagerTheme {
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
            .background(MaterialTheme.colorScheme.background)
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
                    NamagerBottomNavigationBar(
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



@Composable
fun NamagerBottomNavigationBar(
    currentRoute: String?,
    favoriteCount: Int,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
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
                                Badge(containerColor = Flame, contentColor = Color.Black) {
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
            /*    colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = FlameLight,
                    indicatorColor = FlameLight,
                    unselectedIconColor = TextTertiary,
                    unselectedTextColor = TextTertiary
                ),*/
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
            //.statusBarsPadding()
          //  .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("cinema_navigation_rail"),
        //containerColor = DarkSurface
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
                                Badge(containerColor = FlameLight, contentColor = Color.Black) {
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
                    selectedTextColor = FlameLight,
                    indicatorColor = FlameLight,
                    unselectedIconColor = TextTertiary,
                    unselectedTextColor = TextTertiary
                ),
                modifier = Modifier.testTag("rail_item_${destination.route}")
            )
        }
    }
}
