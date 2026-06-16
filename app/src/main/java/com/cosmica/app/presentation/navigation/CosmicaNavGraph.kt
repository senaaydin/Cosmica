package com.cosmica.app.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.cosmica.app.presentation.moonphase.MoonPhaseScreen
import com.cosmica.app.presentation.search.NasaImageDetailScreen
import com.cosmica.app.presentation.search.SearchScreen
import com.cosmica.app.presentation.search.SearchViewModel
import com.cosmica.app.presentation.theme.CosmosBlack
import com.cosmica.app.presentation.theme.GlassSurface
import com.cosmica.app.presentation.theme.OrbitLine
import com.cosmica.app.presentation.theme.SolarOrange

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
                CosmicaBottomDock(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
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
                    },
                    onSearchClick = {
                        navController.navigate(NavRoutes.Search.route)
                    },
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

            composable(NavRoutes.MoonPhase.route) {
                MoonPhaseScreen()
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
                        onBack       = { navController.popBackStack() },
                        onImageClick = { viewModel.selectImage(it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CosmicaBottomDock(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .height(62.dp)
            .clip(RoundedCornerShape(31.dp))
            .background(GlassSurface)
            .border(
                BorderStroke(1.dp, OrbitLine.copy(alpha = 0.7f)),
                RoundedCornerShape(31.dp),
            )
            .padding(horizontal = 8.dp, vertical = 7.dp),
    ) {
        bottomNavItems.forEachIndexed { index, item ->
            val selected = currentRoute == item.route
            CosmicaDockItem(
                item = item,
                selected = selected,
                onClick = { onNavigate(item.route) },
                modifier = Modifier.weight(1f),
            )
            if (index != bottomNavItems.lastIndex) Spacer(Modifier.width(6.dp))
        }
    }
}

@Composable
private fun CosmicaDockItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) SolarOrange.copy(alpha = 0.18f) else Color.Transparent,
        label = "dockContainerColor",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) SolarOrange else MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        label = "dockContentColor",
    )
    val label = stringResource(item.labelResId)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.height(48.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(46.dp)
                .height(46.dp)
                .clip(RoundedCornerShape(23.dp))
                .background(containerColor)
                .clickable(onClick = onClick)
                .padding(8.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
