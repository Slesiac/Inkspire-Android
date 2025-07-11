package com.example.inkspire.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Challenge(
    val id: Int = 0,
    val user_profile_id: String, // Relazione 1-n col model UserProfile
    val title: String,
    val concept: String,
    val art_constraint: String,
    val description: String? = null,
    val result_pic: String? = null, // se presente => challenge completata
    val inserted_at: String? = null,
    val updated_at: String? = null
) : Parcelable