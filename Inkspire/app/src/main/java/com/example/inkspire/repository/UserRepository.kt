package com.example.inkspire.repository

import com.example.inkspire.model.Challenge
import com.example.inkspire.model.UserProfile
import com.example.inkspire.model.UserProfileVW
import com.example.inkspire.supabase.SupabaseManager
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class UserRepository {

    private val client = SupabaseManager.client
    private val auth = SupabaseManager.auth

    // ─────────────── CURRENT USER ID ───────────────
    fun getCurrentUserId(): String? =
        auth.currentUserOrNull()?.id

    // ─────────────── PROFILO ───────────────
    suspend fun getCurrentUserProfile(): UserProfile? {
        val userId = getCurrentUserId() ?: return null
        return getUserProfileById(userId)
    }

    suspend fun getCurrentUsername(): String? {
        return getCurrentUserProfile()?.username
    }

    suspend fun getUserProfileById(userId: String): UserProfile? {
        return try {
            client.from("user_profile").select {
                filter { eq("id", userId) }
                limit(1)
            }.decodeSingleOrNull<UserProfile>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ─────────────── STATISTICHE ───────────────
    suspend fun getUserStats(userId: String): Pair<Int, Int> {
        val total = client.from("challenge")
            .select {
                filter { eq("user_profile_id", userId) }
            }
            .decodeList<Challenge>()
            .size

        val completed = client.from("challenge")
            .select {
                filter {
                    eq("user_profile_id", userId)
                    ilike("result_pic", "%")
                }
            }
            .decodeList<Challenge>()
            .size

        return total to completed
    }

    // ─────────────── UPDATE PROFILE ───────────────

    //Devo passare una Map<String, Any?> per permettere che profile_pic = null venga scritto come NULL nel DB.
    suspend fun updateUserProfile(userProfile: UserProfile): Boolean {
        return try {
            val updateData = mapOf(
                "bio" to userProfile.bio,
                "profile_pic" to userProfile.profile_pic
            )
            client.from("user_profile")
                .update(updateData) {
                    filter { eq("id", userProfile.id) }
                    select()
                }
                .decodeSingleOrNull<UserProfile>() != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

//    suspend fun updateUserProfile(userProfile: UserProfile): Boolean {
//        return try {
//            client.from("user_profile")
//                .update(userProfile) {
//                    filter { eq("id", userProfile.id) }
//                    select()
//                }
//                .decodeSingleOrNull<UserProfile>() != null
//        } catch (e: Exception) {
//            e.printStackTrace()
//            false
//        }
//    }

    // ─────────────── USER LIST ───────────────
    suspend fun getAllUsers(): List<UserProfileVW> { //Tutti gli utenti ordinati per numero di challenge create
        return client.from("user_profile_vw")
            .select {
                order("completed_count", Order.DESCENDING)
            }
            .decodeList<UserProfileVW>()
    }

    suspend fun searchUsers(search: String): List<UserProfileVW> {
        if (search.isEmpty()) return getAllUsers()

        val pattern = "%${search.replace("%", "\\%").replace("_", "\\_")}%"

        return client.from("user_profile_vw")
            .select {
                filter {
                    or {
                        ilike("username", pattern)
                    }
                }
                order("created_count", Order.DESCENDING)
            }
            .decodeList<UserProfileVW>()
    }


}