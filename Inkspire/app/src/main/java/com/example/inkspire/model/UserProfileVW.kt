package com.example.inkspire.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class UserProfileVW(
    val id: String, // Relazione 1-1 con il model UserProfile
    val username: String,
    val profile_pic: String? = null,
    val created_count: Int,
    val completed_count: Int
) : Parcelable