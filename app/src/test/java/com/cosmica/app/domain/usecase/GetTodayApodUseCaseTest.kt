package com.cosmica.app.domain.usecase

import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.repository.ApodRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetTodayApodUseCaseTest {

    private val repository = mockk<ApodRepository>()
    private val useCase    = GetTodayApodUseCase(repository)

    private val fakeApod = Apod(
        date        = "2024-01-01",
        title       = "Test Nebula",
        explanation = "A beautiful nebula.",
        url         = "https://example.com/image.jpg",
        hdUrl       = null,
        mediaType   = "image",
        copyright   = null,
    )

    @Test
    fun `returns success when repository succeeds`() = runTest {
        coEvery { repository.getTodayApod() } returns Result.success(fakeApod)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(fakeApod, result.getOrNull())
        coVerify(exactly = 1) { repository.getTodayApod() }
    }

    @Test
    fun `returns failure when repository throws`() = runTest {
        val error = RuntimeException("Network error")
        coEvery { repository.getTodayApod() } returns Result.failure(error)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
