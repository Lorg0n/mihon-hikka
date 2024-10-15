package eu.kanade.tachiyomi.data.track.hikka.dto

import kotlinx.serialization.Serializable

@Serializable
data class HKClientResponse(
    val reference: String,
    val name: String,
    val description: String,
    val verified: Boolean,
    val user: HKUserResponse,
    val created: Long,
    val updated: Long
)
