package com.cosmica.app.domain.usecase

import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.repository.ApodRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ToggleFavoriteUseCaseTest {

    private val repository = mockk<ApodRepository>(relaxed = true)
    private val useCase    = ToggleFavoriteUseCase(repository)

    private val apod = Apod(
        date = "2024-01-01", title = "Test", explanation = "Exp",
        url = "https://example.com", hdUrl = null, mediaType = "image", copyright = null,
    )

    @Test
    fun `adds favorite when not already saved`() = runTest {
        coEvery { repository.isFavorite(apod.date) } returns false

        useCase(apod)

        coVerify { repository.addFavorite(apod) }
        coVerify(exactly = 0) { repository.removeFavorite(any()) }
    }

    @Test
    fun `removes favorite when already saved`() = runTest {
        coEvery { repository.isFavorite(apod.date) } returns true

        useCase(apod)

        coVerify { repository.removeFavorite(apod.date) }
        coVerify(exactly = 0) { repository.addFavorite(any()) }
    }
}
