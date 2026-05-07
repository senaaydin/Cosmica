package com.cosmica.app.data.mapper

import com.cosmica.app.data.remote.dto.NeoDto
import com.cosmica.app.data.remote.dto.NeoWsResponseDto
import com.cosmica.app.domain.model.NearEarthObject

fun NeoWsResponseDto.toDomain(): List<NearEarthObject> =
    nearEarthObjects.values.flatten().map { it.toDomain() }

fun NeoDto.toDomain(): NearEarthObject {
    val approach = closeApproachData.firstOrNull()
    return NearEarthObject(
        id                    = id,
        name                  = name.removeSurrounding("(", ")"),
        absoluteMagnitude     = absoluteMagnitudeH,
        estimatedDiameterMinKm = estimatedDiameter.kilometers.min,
        estimatedDiameterMaxKm = estimatedDiameter.kilometers.max,
        isPotentiallyHazardous = isPotentiallyHazardous,
        closeApproachDate     = approach?.date ?: "",
        relativeVelocityKph   = approach?.relativeVelocity?.kilometersPerHour?.toDoubleOrNull() ?: 0.0,
        missDistanceKm        = approach?.missDistance?.kilometers?.toDoubleOrNull() ?: 0.0,
        missDistanceLunar     = approach?.missDistance?.lunar?.toDoubleOrNull() ?: 0.0,
        orbitingBody          = approach?.orbitingBody ?: "Earth",
    )
}
