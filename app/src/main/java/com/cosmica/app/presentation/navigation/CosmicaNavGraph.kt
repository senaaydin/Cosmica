package com.cosmica.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cosmica.app.presentation.asteroids.AsteroidDetailScreen
import com.cosmica.app.presentation.asteroids.AsteroidsScreen
import com.cosmica.app.presentation.asteroids.AsteroidsViewModel
import com.cosmica.app.presentation.common.ScreenUiState
import com.cosmica.app.presentation.favorites.FavoritesScreen
import com.cosmica.app.presentation.gallery.ApodDetailScreen
import com.cosmica.app.presentation.gallery.GalleryScreen
import com.cosmica.app.presentation.home.HomeScreen
import com.cosmica.app.presentation.search.NasaImageDetailScreen
import com.cosmica.app.presentation.search.SearchScreen
import com.cosmica.app.presentation.search.SearchViewModel
import com.cosmica.app.presentation.theme.CosmosBlack
import com.cosmica.app.presentation.theme.DeepNavy

@Composable
fun CosmicaNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topLevelRoutes = bottomNavItems.map { it.route }
    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        containerColor = CosmosBlack,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = DeepNavy) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick  = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = { Icon(item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.labelResId)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor   = MaterialTheme.colorScheme.primary,
                                selectedTextColor   = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor      = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = NavRoutes.Home.route,
            modifier         = Modifier.padding(innerPadding),
        ) {
            composable(NavRoutes.Home.route) {
                HomeScreen()
            }

            composable(NavRoutes.Gallery.route) {
                GalleryScreen(
                    onApodClick = { date ->
                        navController.navigate(NavRoutes.ApodDetail.createRoute(date))
                    }
                )
            }

            composable(
                route     = NavRoutes.ApodDetail.route,
                arguments = listOf(navArgument(NavRoutes.ApodDetail.ARG_DATE) { type = NavType.StringType }),
            ) { backStackEntry ->
                val date = backStackEntry.arguments?.getString(NavRoutes.ApodDetail.ARG_DATE) ?: return@composable
                ApodDetailScreen(date = date, onBack = { navController.popBackStack() })
            }

            composable(NavRoutes.Favorites.route) {
                FavoritesScreen(
                    onApodClick = { date ->
                        navController.navigate(NavRoutes.ApodDetail.createRoute(date))
                    }
                )
            }

            composable(NavRoutes.Asteroids.route) {
                val viewModel: AsteroidsViewModel = hiltViewModel()
                val selectedAsteroid by viewModel.selectedAsteroid.collectAsStateWithLifecycle()

                if (selectedAsteroid != null) {
                    AsteroidDetailScreen(
                        neo    = selectedAsteroid!!,
                        onBack = { viewModel.clearSelection() },
                    )
                } else {
                    AsteroidsScreen(
                        onAsteroidClick = { id ->
                            val state = viewModel.uiState.value
                            if (state is ScreenUiState.Success) {
                                state.data.find { it.id == id }?.let { viewModel.selectAsteroid(it) }
                            }
                        },
                        viewModel = viewModel,
                    )
                }
            }

            composable(NavRoutes.Search.route) {
                val viewModel: SearchViewModel = hiltViewModel()
                val selectedImage by viewModel.selectedImage.collectAsStateWithLifecycle()

                if (selectedImage != null) {
                    NasaImageDetailScreen(
                        image  = selectedImage!!,
                        onBack = { viewModel.clearSelectedImage() },
                    )
                } else {
                    SearchScreen(
                        viewModel    = viewModel,
                        onImageClick = { viewModel.selectImage(it) },
                    )
                }
            }
        }
    }
}
