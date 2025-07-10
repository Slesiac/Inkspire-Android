package com.example.inkspire.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class UserProfile(
    val id: String, // (UUID) corrisponde al campo "id" nella tabella auth.users di Supabase
    val username: String,
    val profile_pic: String? = null,
    val bio: String? = null
) : Parcelable

//@Serializable
//data class UserProfile(
//    val id: String,
//    val username: String
//)