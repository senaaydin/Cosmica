package com.cosmica.app.domain.repository

import com.cosmica.app.domain.model.NearEarthObject

interface NeoRepository {
    suspend fun getAsteroidsThisWeek(): Result<List<NearEarthObject>>
}
