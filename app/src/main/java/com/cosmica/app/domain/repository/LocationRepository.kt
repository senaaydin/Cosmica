package com.cosmica.app.domain.repository

import com.cosmica.app.domain.model.Coordinates

interface LocationRepository {
    /** Caller is responsible for ensuring location permission is granted before calling. */
    suspend fun getCurrentLocation(): Result<Coordinates>
}
