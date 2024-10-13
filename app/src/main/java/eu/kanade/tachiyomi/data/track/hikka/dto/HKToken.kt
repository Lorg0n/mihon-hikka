package eu.kanade.tachiyomi.data.track.hikka.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HKAuthResponseDto(
    @SerialName("reference")
    val reference: String,
    @SerialName("created")
    val created: Long,
    @SerialName("client")
    val client: HKClientDto,
    @SerialName("scope")
    val scope: List<String>,
    @SerialName("expiration")
    val expiration: Long,
    @SerialName("used")
    val used: Long,
)

@Serializable
data class HKClientDto(
    @SerialName("reference")
    val reference: String,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String,
    @SerialName("verified")
    val verified: Boolean,
    @SerialName("user")
    val user: HKUserDto,
    @SerialName("created")
    val created: Long,
    @SerialName("updated")
    val updated: Long
)

@Serializable
data class HKUserDto(
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
