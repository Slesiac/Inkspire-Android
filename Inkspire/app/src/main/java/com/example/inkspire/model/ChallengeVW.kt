package com.example.inkspire.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

//Model riferito alla view "challenge_vw" fatta su supabase per la join tra le tabelle "challenge" e "user_profile"
//Necessario per gli elenchi nelle recycler view personalizzate
@Parcelize
@Serializable
data class ChallengeVW(
    val id: Int, // Relazione 1-1 con il model Challenge
    val user_id: String, // UUID - corrisponde al campo "id" nella tabella auth.users di Supabase
    val title: String,
    val concept: String,
    val art_constraint: String,
    val description: String? = null,
    val result_pic: String? = null,
    val inserted_at: String? = null,
    val updated_at: String? = null,
    val username: String,
    val profile_pic: String? = null,
    val bio: String? = null
) : Parcelable

/*
@Parcelize
@Serializable
data class ChallengeUser(
    val id: Int,
    val user_profile_id: String,
    val title: String,
    val concept: String,
    val art_constraint: String,
    val description: String? = null,
    val result_pic: String? = null,
    val inserted_at: String? = null,
    val updated_at: String? = null,
    val user_profile: UserProfile,
    val isCompleted: Boolean = false
) : Parcelable

// Funzione che permette di ottenere facilmente un oggetto Challenge da un ChallengeUser
// (es. per riutilizzare metodi che richiedono solo la challenge pura)
fun ChallengeUser.toChallenge(): Challenge {
    return Challenge(
        id = this.id,
        user_profile_id = this.user_profile_id,
        title = this.title,
        concept = this.concept,
        art_constraint = this.art_constraint,
        description = this.description,
        result_pic = this.result_pic,
        inserted_at = this.inserted_at,
        updated_at = this.updated_at
    )
}*/