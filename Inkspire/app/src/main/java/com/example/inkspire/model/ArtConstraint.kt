package com.example.inkspire.model

import kotlinx.serialization.Serializable

@Serializable
data class ArtConstraint(
    val id: Int,
    val art_constraint: String
)