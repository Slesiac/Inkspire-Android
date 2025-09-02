package com.example.inkspire

import com.example.inkspire.model.Challenge
import kotlinx.coroutines.delay

// Fake repository usato per testare la logica senza connettersi a Supabase.
// Tiene i dati in memoria dentro una lista mutabile.
class FakeChallengeRepository {

    private val challenges = mutableListOf<Challenge>()
    private var autoIncrementId = 1

    fun insertChallenge(challenge: Challenge): Boolean {
        val newChallenge = challenge.copy(id = autoIncrementId++)
        challenges.add(newChallenge)
        return true
    }

    fun updateChallenge(challenge: Challenge): Boolean {
        val index = challenges.indexOfFirst { it.id == challenge.id }
        return if (index != -1) {
            challenges[index] = challenge
            true
        } else {
            false
        }
    }

    fun deleteChallenge(challengeId: Int): Boolean {
        return challenges.removeIf { it.id == challengeId }
    }

    fun getChallengeById(id: Int): Challenge? {
        return challenges.find { it.id == id }
    }

    fun getChallengesByUser(userId: String): List<Challenge> {
        return challenges.filter { it.user_profile_id == userId }
    }

    suspend fun getAllChallenges(): List<Challenge> {
        delay(10) // Simula un leggero ritardo come se fosse una query
        return challenges.toList()
    }

    fun searchChallenges(search: String): List<Challenge> {
        return challenges.filter {
            it.title.contains(search, ignoreCase = true) ||
                    it.concept.contains(search, ignoreCase = true) ||
                    it.art_constraint.contains(search, ignoreCase = true) ||
                    (it.description?.contains(search, ignoreCase = true) == true)
        }
    }

    private val concepts = listOf("Lonely tree", "Sunset desert", "Wildflowers", "Enchanted forest")

    fun getRandomConcept(): String? {
        return concepts.randomOrNull()
    }
}