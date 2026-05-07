package com.cosmica.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NeoWsResponseDto(
    @SerializedName("near_earth_objects")
    val nearEarthObjects: Map<String, List<NeoDto>>,
)

data class NeoDto(
    @SerializedName("id")                               val id: String,
    @SerializedName("name")                             val name: String,
    @SerializedName("absolute_magnitude_h")             val absoluteMagnitudeH: Double,
    @SerializedName("estimated_diameter")               val estimatedDiameter: EstimatedDiameterDto,
    @SerializedName("is_potentially_hazardous_asteroid") val isPotentiallyHazardous: Boolean,
    @SerializedName("close_approach_data")              val closeApproachData: List<CloseApproachDto>,
)

data class EstimatedDiameterDto(
    @SerializedName("kilometers") val kilometers: DiameterRangeDto,
)

data class DiameterRangeDto(
    @SerializedName("estimated_diameter_min") val min: Double,
    @SerializedName("estimated_diameter_max") val max: Double,
)

data class CloseApproachDto(
    @SerializedName("close_approach_date") val date: String,
    @SerializedName("relative_velocity")   val relativeVelocity: RelativeVelocityDto,
    @SerializedName("miss_distance")       val missDistance: MissDistanceDto,
    @SerializedName("orbiting_body")       val orbitingBody: String,
)

data class RelativeVelocityDto(
    @SerializedName("kilometers_per_hour") val kilometersPerHour: String,
)

data class MissDistanceDto(
    @SerializedName("kilometers") val kilometers: String,
    @SerializedName("lunar")      val lunar: String,
)
