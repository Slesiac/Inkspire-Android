package com.example.inkspire

import com.example.inkspire.model.Challenge
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ChallengeRepositoryTest {

    private lateinit var repository: FakeChallengeRepository

    @Before
    fun setUp() {
        repository = FakeChallengeRepository()
    }

    @Test
    fun insertChallenge_shouldAddChallenge() = runBlocking {
        val challenge = Challenge(
            id = 0,
            user_profile_id = "user1",
            title = "Test Title",
            concept = "Concept",
            art_constraint = "Constraint"
        )

        val result = repository.insertChallenge(challenge)
        val all = repository.getAllChallenges()

        assertTrue(result)
        assertEquals(1, all.size)
        assertEquals("Test Title", all[0].title)
    }

    @Test
    fun updateChallenge_shouldModifyExistingChallenge() = runBlocking {
        val challenge = Challenge(
            id = 0,
            user_profile_id = "user1",
            title = "Old Title",
            concept = "Concept",
            art_constraint = "Constraint"
        )
        repository.insertChallenge(challenge)
        val inserted = repository.getAllChallenges().first()

        val updated = inserted.copy(title = "New Title")
        val result = repository.updateChallenge(updated)
        val reloaded = repository.getChallengeById(inserted.id)

        assertTrue(result)
        assertEquals("New Title", reloaded?.title)
    }

    @Test
    fun deleteChallenge_shouldRemoveChallenge() = runBlocking {
        val challenge = Challenge(
            id = 0,
            user_profile_id = "user1",
            title = "Delete Me",
            concept = "Concept",
            art_constraint = "Constraint"
        )
        repository.insertChallenge(challenge)
        val inserted = repository.getAllChallenges().first()

        val result = repository.deleteChallenge(inserted.id)
        val all = repository.getAllChallenges()

        assertTrue(result)
        assertTrue(all.isEmpty())
    }

    @Test
    fun getChallengesByUser_shouldReturnOnlyMatchingUser() = runBlocking {
        repository.insertChallenge(Challenge(0, "user1", "Title1", "Concept", "Constraint"))
        repository.insertChallenge(Challenge(0, "user2", "Title2", "Concept", "Constraint"))

        val user1Challenges = repository.getChallengesByUser("user1")

        assertEquals(1, user1Challenges.size)
        assertEquals("user1", user1Challenges[0].user_profile_id)
    }

    @Test
    fun searchChallenges_shouldReturnMatchingResults() = runBlocking {
        repository.insertChallenge(Challenge(0, "user1", "Landscape", "Nature", "Constraint"))
        repository.insertChallenge(Challenge(0, "user2", "Portrait", "Human", "Constraint"))

        val results = repository.searchChallenges("land")

        assertEquals(1, results.size)
        assertEquals("Landscape", results[0].title)
    }
}