@file:OptIn(org.jetbrains.compose.resources.ExperimentalResourceApi::class)
package com.mahmoud.azkarapp

import azkarapp.shared.generated.resources.Res
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

class AzkarRepository {
    private val json = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadAzkar(): List<Zekr> {
        return try {
            val bytes = Res.readBytes("files/azkar.json")
            json.decodeFromString(bytes.decodeToString())
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadHadiths(): List<Hadith> {
        return try {
            val bytes = Res.readBytes("files/hadith.json")
            json.decodeFromString(bytes.decodeToString())
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
