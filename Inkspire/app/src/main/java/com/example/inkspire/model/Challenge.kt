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

//@Parcelize
//@Serializable
//data class Challenge(
//    val id: Int = 0,
//    val user_profile_id: String, // user_profile_id UUID REFERENCES user_profile(id)
//    val title: String,
//    val concept: String,
//    val art_constraint: String,
//    val description: String? = null,
//    val result_pic: String? = null, // se presente => challenge completata
//    val inserted_at: String? = null,
//    val updated_at: String? = null
//) : Parcelable {
//    val isCompleted: Boolean
//        get() = !result_pic.isNullOrBlank()
//}

//@Parcelize
//@Serializable
//data class Challenge(
//    val id: Int = 0,
//    val title: String,
//    val subject: String,
//    val description: String,
//    val created_by: String? = null,
//    val created_at: String? = null,
//    val updated_at: String? = null
//) : Parcelable