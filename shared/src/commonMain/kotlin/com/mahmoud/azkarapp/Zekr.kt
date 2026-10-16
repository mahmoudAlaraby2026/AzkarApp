package com.mahmoud.azkarapp

import kotlinx.serialization.Serializable

@Serializable
data class Zekr(
    val id: Int,
    val category: String,
    val text: String,
    val count: Int,
    val audio: String = ""
)