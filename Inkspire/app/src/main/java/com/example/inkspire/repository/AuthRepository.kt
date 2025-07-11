package com.example.inkspire.repository

import com.example.inkspire.supabase.SupabaseManager
import com.example.inkspire.model.UserProfile
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from

class AuthRepository {

    private val auth = SupabaseManager.auth
    private val client = SupabaseManager.client

    suspend fun signUp(email: String, password: String, username: String): Result<Unit> {
        return try {
            // Step 1: Controlla se lo username è già stato usato
            val existingProfiles = client
                .from("user_profile")
                .select {
                    filter {
                        eq("username", username)
                    }
                }
                .decodeList<UserProfile>()

            if (existingProfiles.isNotEmpty()) {
                return Result.failure(Exception("Username already taken"))
            }

            // Step 2: Registra l'utente con Supabase Auth
            val signUpResult = auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val userId = auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Sign up failed: User ID is null"))

            // Step 3: Inserisci il profilo personalizzato
            val profile = UserProfile(id = userId, username = username)
            client
                .from("user_profile")
                .insert(profile)

            Result.success(Unit)

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun logout() {
        auth.signOut()
    }

    // Stato utente
    fun isLoggedIn(): Boolean = auth.currentUserOrNull() != null
    fun currentUser(): UserInfo? = auth.currentUserOrNull()

    // Profilo dell'utente corrente
    suspend fun getCurrentUserProfile(): UserProfile? {
        val userId = auth.currentUserOrNull()?.id ?: return null
        return getUserProfileByUserId(userId)
    }

    // Profilo di un qualsiasi utente dato l'uuid
    suspend fun getUserProfileByUserId(userId: String): UserProfile? {
        return try {
            client.from("user_profile").select {
                filter { eq("id", userId) }
            }.decodeSingle<UserProfile>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}