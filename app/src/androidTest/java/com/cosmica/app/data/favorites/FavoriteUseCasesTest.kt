package com.cosmica.app.data.favorites

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.cosmica.app.data.local.CosmicaDatabase
import com.cosmica.app.data.repository.FavoriteRepositoryImpl
import com.cosmica.app.domain.model.Apod
import com.cosmica.app.domain.usecase.AddFavoriteUseCase
import com.cosmica.app.domain.usecase.GetAllFavoritesUseCase
import com.cosmica.app.domain.usecase.IsFavoriteUseCase
import com.cosmica.app.domain.usecase.RemoveFavoriteUseCase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteUseCasesTest {

    private lateinit var db: CosmicaDatabase
    private lateinit var addFavorite: AddFavoriteUseCase
    private lateinit var removeFavorite: RemoveFavoriteUseCase
    private lateinit var getAllFavorites: GetAllFavoritesUseCase
    private lateinit var isFavorite: IsFavoriteUseCase

    private val apod = Apod(
        date        = "2024-01-25",
        title       = "Wolf Moon",
        explanation = "January full moon",
        url         = "https://apod.nasa.gov/wolf.jpg",
        hdUrl       = null,
        mediaType   = "image",
        copyright   = "NASA",
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, CosmicaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = FavoriteRepositoryImpl(db.favoriteApodDao())
        addFavorite     = AddFavoriteUseCase(repository)
        removeFavorite  = RemoveFavoriteUseCase(repository)
        getAllFavorites = GetAllFavoritesUseCase(repository)
        isFavorite      = IsFavoriteUseCase(repository)
    }

    @After fun tearDown() { db.close() }

    @Test
    fun add_then_getAll_returns_inserted_apod() = runTest {
        addFavorite(apod)

        getAllFavorites().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(apod.date, items.first().date)
            assertEquals(apod.explanation, items.first().explanation)
        }
    }

    @Test
    fun isFavorite_emits_true_after_add_and_false_after_remove() = runTest {
        isFavorite(apod.date).test {
            assertFalse(awaitItem())

            addFavorite(apod)
            assertTrue(awaitItem())

            removeFavorite(apod.date)
            assertFalse(awaitItem())
        }
    }

    @Test
    fun remove_actually_deletes_the_row() = runTest {
        addFavorite(apod)
        removeFavorite(apod.date)

        getAllFavorites().test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun add_is_idempotent_via_replace_strategy() = runTest {
        addFavorite(apod)
        addFavorite(apod.copy(title = "Updated Title"))

        getAllFavorites().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals("Updated Title", items.first().title)
        }
    }
}
