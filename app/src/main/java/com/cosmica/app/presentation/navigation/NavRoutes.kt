package com.cosmica.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Rocket
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavRoutes(val route: String) {
    data object Home      : NavRoutes("home")
    data object Gallery   : NavRoutes("gallery")
    data object Favorites : NavRoutes("favorites")
    data object Asteroids : NavRoutes("asteroids")
    data object Search    : NavRoutes("search")

    data object ApodDetail : NavRoutes("apod_detail/{date}") {
        fun createRoute(date: String) = "apod_detail/$date"
        const val ARG_DATE = "date"
    }

    data object AsteroidDetail : NavRoutes("asteroid_detail/{asteroidId}") {
        fun createRoute(id: String) = "asteroid_detail/$id"
        const val ARG_ID = "asteroidId"
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val labelResId: Int,
)

val bottomNavItems = listOf(
    BottomNavItem(NavRoutes.Home.route,      Icons.Rounded.Home,         com.cosmica.app.R.string.nav_home),
    BottomNavItem(NavRoutes.Gallery.route,   Icons.Rounded.PhotoLibrary, com.cosmica.app.R.string.nav_gallery),
    BottomNavItem(NavRoutes.Favorites.route, Icons.Rounded.Favorite,     com.cosmica.app.R.string.nav_favorites),
    BottomNavItem(NavRoutes.Asteroids.route, Icons.Rounded.Rocket,       com.cosmica.app.R.string.nav_asteroids),
    BottomNavItem(NavRoutes.Search.route,    Icons.Rounded.Search,       com.cosmica.app.R.string.nav_search),
)
