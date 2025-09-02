package com.example.inkspire

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RandomConceptTest {

    private lateinit var repo: FakeChallengeRepository

    @Before
    fun setUp() {
        repo = FakeChallengeRepository()
    }

    @Test
    fun testRandomConceptIsValid() = runBlocking {
        val concept = repo.getRandomConcept()
        val validConcepts = listOf("Animals", "Nature", "Fantasy", "Sci-Fi")

        // Verifica che il valore ottenuto sia uno di quelli attesi
        assertTrue(validConcepts.contains(concept))
    }
}