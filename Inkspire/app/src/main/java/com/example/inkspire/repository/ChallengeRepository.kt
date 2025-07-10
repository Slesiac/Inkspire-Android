package com.example.inkspire.repository

import com.example.inkspire.model.ArtConstraint
import com.example.inkspire.model.Challenge
import com.example.inkspire.model.ChallengeVW
import com.example.inkspire.model.Concept
import com.example.inkspire.supabase.SupabaseManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class ChallengeRepository {

    private val supabase: SupabaseClient = SupabaseManager.client

    suspend fun insertChallenge(challenge: Challenge): Boolean {
        return try {
            supabase.from("challenge")
                .insert(challenge) {
                    select()
                }
                .decodeSingle<Challenge>()
            true
        } catch (e: Exception) {
            false
        }
    }

//    suspend fun updateChallenge(challenge: Challenge): Boolean {
//        return try {
//            supabase.from("challenge")
//                .update(challenge) {
//                    filter { eq("id", challenge.id) }
//                    select()
//                }
//                .decodeSingle<Challenge>()
//            true
//        } catch (e: Exception) {
//            false
//        }
//    }

    //Devo passare una Map<String, Any?> per permettere che result_pic = null venga scritto come NULL nel DB.
    suspend fun updateChallenge(challenge: Challenge): Boolean {
        return try {
            val updateData = mapOf(
                "title" to challenge.title,
                "concept" to challenge.concept,
                "art_constraint" to challenge.art_constraint,
                "description" to challenge.description,
                "result_pic" to challenge.result_pic,
                "updated_at" to challenge.updated_at
            )
            supabase.from("challenge")
                .update(updateData) {
                    filter { eq("id", challenge.id) }
                    select()
                }
                .decodeSingle<Challenge>()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteChallenge(challengeId: Int): Boolean {
        return try {
            supabase.from("challenge")
                .delete {
                    filter { eq("id", challengeId) }
                }
            true
        } catch (e: Exception) {
            false
        }
    }

    // Ottieni un concept casuale
    suspend fun getRandomConcept(): String? {
        return supabase.from("concept")
            .select()
            .decodeList<Concept>()
            .randomOrNull()
            ?.concept
    }

    // Ottieni un art constraint casuale
    suspend fun getRandomArtConstraint(): String? {
        return supabase.from("art_constraint")
            .select()
            .decodeList<ArtConstraint>()
            .randomOrNull()
            ?.art_constraint
    }

    suspend fun getAllChallenges(): List<ChallengeVW> {
        return supabase.from("challenge_vw")
            .select {
                order("updated_at", Order.DESCENDING)
            }
            .decodeList<ChallengeVW>()
    }

    suspend fun searchChallenges(search: String): List<ChallengeVW> {
        if (search.isEmpty()) return getAllChallenges()

        val pattern = "%${search.replace("%", "\\%").replace("_", "\\_")}%"

        return supabase.from("challenge_vw")
            .select {
                filter {
                    or {
                        ilike("title", pattern)
                        ilike("description", pattern)
                        ilike("concept", pattern)
                        ilike("art_constraint", pattern)
                        ilike("username", pattern)
                    }
                }
                order("updated_at", Order.DESCENDING)
            }
            .decodeList<ChallengeVW>()
    }

    suspend fun getChallengeById(id: Int): ChallengeVW? {
        return supabase.from("challenge_vw")
            .select {
                filter { eq("id", id) }
                limit(1)
            }
            .decodeSingleOrNull()
    }

    suspend fun getChallengesByUser(userId: String): List<ChallengeVW> {
        return supabase.from("challenge_vw")
            .select {
                filter { eq("user_id", userId) }
                order("updated_at", Order.DESCENDING)
            }
            .decodeList<ChallengeVW>()
    }
}