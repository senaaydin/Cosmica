package com.cosmica.app.data.mapper

import com.cosmica.app.data.remote.dto.CloseApproachDto
import com.cosmica.app.data.remote.dto.DiameterRangeDto
import com.cosmica.app.data.remote.dto.EstimatedDiameterDto
import com.cosmica.app.data.remote.dto.MissDistanceDto
import com.cosmica.app.data.remote.dto.NeoDto
import com.cosmica.app.data.remote.dto.NeoWsResponseDto
import com.cosmica.app.data.remote.dto.RelativeVelocityDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class NeoMapperTest {

    private val fakeDto = NeoDto(
        id                    = "3542519",
        name                  = "(2010 PK9)",
        absoluteMagnitudeH    = 22.1,
        estimatedDiameter     = EstimatedDiameterDto(
            kilometers = DiameterRangeDto(min = 0.0627, max = 0.1402)
        ),
        isPotentiallyHazardous = false,
        closeApproachData     = listOf(
            CloseApproachDto(
                date             = "2024-01-10",
                relativeVelocity = RelativeVelocityDto(kilometersPerHour = "45000.0"),
                missDistance     = MissDistanceDto(kilometers = "2000000.0", lunar = "5.2"),
                orbitingBody     = "Earth",
            )
        ),
    )

    @Test
    fun `toDomain strips parentheses from name`() {
        val domain = fakeDto.toDomain()
        assertEquals("2010 PK9", domain.name)
    }

    @Test
    fun `toDomain maps velocity and distance correctly`() {
        val domain = fakeDto.toDomain()
        assertEquals(45000.0, domain.relativeVelocityKph, 0.001)
        assertEquals(2_000_000.0, domain.missDistanceKm, 0.001)
        assertEquals(5.2, domain.missDistanceLunar, 0.001)
    }

    @Test
    fun `toDomain marks non-hazardous correctly`() {
        val domain = fakeDto.toDomain()
        assertFalse(domain.isPotentiallyHazardous)
    }

    @Test
    fun `NeoWsResponseDto toDomain flattens all date buckets`() {
        val response = NeoWsResponseDto(
            nearEarthObjects = mapOf(
                "2024-01-10" to listOf(fakeDto),
                "2024-01-11" to listOf(fakeDto.copy(id = "9999")),
            )
        )
        val list = response.toDomain()
        assertEquals(2, list.size)
    }
}
