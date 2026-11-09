package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.AnkitVerseRepository
import com.example.data.session.SessionManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: AnkitVerseRepository
    private lateinit var sessionManager: SessionManager

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessionManager = SessionManager(context)
        repository = AnkitVerseRepository(db, sessionManager)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AnkitVerse", appName)
    }

    @Test
    fun `user registration with valid credentials succeeds and creates persistent session`() = runBlocking {
        val result = repository.registerUser(
            username = "ankit_creator",
            displayName = "Ankit Sharma",
            age = 22,
            password = "securePassword123",
            avatarUri = "content://media/avatar.jpg"
        )
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertNotNull(user)
        assertEquals("ankit_creator", user?.username)
        assertEquals(sessionManager.currentUserId, user?.id)
        assertTrue(sessionManager.isLoggedIn)
    }

    @Test
    fun `duplicate username registration fails`() = runBlocking {
        repository.registerUser(
            username = "ankit_creator",
            displayName = "Ankit Sharma",
            age = 22,
            password = "securePassword123",
            avatarUri = "content://media/avatar.jpg"
        )

        val duplicateResult = repository.registerUser(
            username = "ankit_creator",
            displayName = "Another User",
            age = 25,
            password = "anotherPassword",
            avatarUri = "content://media/avatar2.jpg"
        )
        assertTrue(duplicateResult.isFailure)
    }

    @Test
    fun `underage user registration fails`() = runBlocking {
        val underageResult = repository.registerUser(
            username = "young_kid",
            displayName = "Kid",
            age = 11,
            password = "password123",
            avatarUri = "content://media/avatar.jpg"
        )
        assertTrue(underageResult.isFailure)
    }

    @Test
    fun `monetization threshold calculation matches 500k views`() {
        assertEquals(500_000L, AnkitVerseRepository.MONETIZATION_VIEW_THRESHOLD)
    }
}
