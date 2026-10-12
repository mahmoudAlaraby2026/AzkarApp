package com.mahmoud.azkarapp

import kotlinx.serialization.Serializable

@Serializable
data class Hadith(
    val id: Int,
    val text: String,
    val explanation: String
)
