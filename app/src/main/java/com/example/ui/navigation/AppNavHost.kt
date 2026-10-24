package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.player.PlayerScreen
import com.example.ui.screens.DetailsScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.MoviesScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SeriesScreen
import com.example.ui.screens.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.MOVIES,
        modifier = modifier
    ) {
        composable(AppRoutes.MOVIES) {
            MoviesScreen(
                onMediaClick = { media -> navController.navigate(AppRoutes.details(media.id)) },
                onSearchClick = { navController.navigateTopLevel(AppRoutes.SEARCH) }
            )
        }

        composable(AppRoutes.SERIES) {
            SeriesScreen(
                onMediaClick = { media -> navController.navigate(AppRoutes.details(media.id)) },
                onSearchClick = { navController.navigateTopLevel(AppRoutes.SEARCH) }
            )
        }

        composable(AppRoutes.SEARCH) {
            SearchScreen(
                onMediaClick = { media -> navController.navigate(AppRoutes.details(media.id)) }
            )
        }

        composable(AppRoutes.FAVORITES) {
            FavoritesScreen(
                onMediaClick = { media -> navController.navigate(AppRoutes.details(media.id)) }
            )
        }

        composable(AppRoutes.SETTINGS) {
            SettingsScreen()
        }

        composable(
            route = AppRoutes.DETAILS,
            arguments = listOf(
                navArgument("mediaId") { type = NavType.IntType }
            )
        ) {
            DetailsScreen(
                onBack = { navController.popBackStack() },
                onPlay = { url, title, subtitle, mediaId, seasonId, episodeId ->
                    navController.navigate(
                        AppRoutes.player(url, title, subtitle, mediaId, seasonId, episodeId)
                    )
                }
            )
        }

        composable(
            route = AppRoutes.PLAYER,
            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("title") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("subtitle") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("mediaId") {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("seasonId") {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("episodeId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) {
            PlayerScreen(onClose = { navController.popBackStack() })
        }
    }
}

fun NavHostController.navigateTopLevel(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
