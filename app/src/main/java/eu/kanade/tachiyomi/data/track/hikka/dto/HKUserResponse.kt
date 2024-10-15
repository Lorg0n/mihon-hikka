package eu.kanade.tachiyomi.data.track.hikka.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HKUserResponse(
    @SerialName("reference")
    val reference: String,

    @SerialName("updated")
    val updated: Long,

    @SerialName("created")
    val created: Long,

    @SerialName("description")
    val description: String,

    @SerialName("username")
    val username: String,

    @SerialName("cover")
    val cover: String,

    @SerialName("active")
    val active: Boolean,

    @SerialName("avatar")
    val avatar: String,

    @SerialName("role")
    val role: String
)
