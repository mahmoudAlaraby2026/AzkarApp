package com.mahmoud.azkarapp

import azkarapp.shared.generated.resources.Res
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

class AzkarRepository {
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadAzkar(): List<Zekr> {
        val bytes = Res.readBytes("files/azkar.json")
        return json.decodeFromString(bytes.decodeToString())
    }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadHadiths(): List<Hadith> {
        val bytes = Res.readBytes("files/hadith.json")
        return json.decodeFromString(bytes.decodeToString())
    }
}
