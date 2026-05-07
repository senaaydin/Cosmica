package com.cosmica.app.domain.model

import java.text.NumberFormat
import java.util.Locale

data class NearEarthObject(
    val id: String,
    val name: String,
    val absoluteMagnitude: Double,
    val estimatedDiameterMinKm: Double,
    val estimatedDiameterMaxKm: Double,
    val isPotentiallyHazardous: Boolean,
    val closeApproachDate: String,
    val relativeVelocityKph: Double,
    val missDistanceKm: Double,
    val missDistanceLunar: Double,
    val orbitingBody: String,
) {
    val formattedVelocity: String
        get() = NumberFormat.getNumberInstance(Locale.US).format(relativeVelocityKph.toLong())

    val formattedMissDistanceKm: String
        get() = NumberFormat.getNumberInstance(Locale.US).format(missDistanceKm.toLong())
}
