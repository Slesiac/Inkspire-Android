package com.example.inkspire.model

import kotlinx.serialization.Serializable

@Serializable
data class Concept(
    val id: Int,
    val concept: String
)