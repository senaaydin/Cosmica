package com.cosmica.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavRoutes(val route: String) {
    data object Home      : NavRoutes("home")
    data object Gallery   : NavRoutes("gallery")
    data object Favorites : NavRoutes("favorites")
    data object Asteroids : NavRoutes("asteroids")
    data object Search    : NavRoutes("search")
    data object MoonPhase : NavRoutes("moon_phase")

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
    BottomNavItem(NavRoutes.Home.route,      Icons.Outlined.Public,         com.cosmica.app.R.string.nav_home),
    BottomNavItem(NavRoutes.Gallery.route,   Icons.Outlined.Collections,    com.cosmica.app.R.string.nav_gallery),
    BottomNavItem(NavRoutes.Favorites.route, Icons.Outlined.FavoriteBorder, com.cosmica.app.R.string.nav_favorites),
    BottomNavItem(NavRoutes.Asteroids.route, Icons.Outlined.RocketLaunch,   com.cosmica.app.R.string.nav_asteroids),
    BottomNavItem(NavRoutes.MoonPhase.route, Icons.Outlined.NightsStay,     com.cosmica.app.R.string.nav_moon_phase),
)
