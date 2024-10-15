package eu.kanade.tachiyomi.data.track.hikka.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HKTokenInfo(
    @SerialName("reference")
    val reference: String,

    @SerialName("created")
    val created: Long,

    @SerialName("client")
    val client: HKClient,

    @SerialName("scope")
    val scope: List<String>,

    @SerialName("expiration")
    val expiration: Long,

    @SerialName("used")
    val used: Long
)

@Serializable
data class HKClient(
    @SerialName("reference")
    val reference: String,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String,

    @SerialName("verified")
    val verified: Boolean,

    @SerialName("user")
    val user: HKUser,

    @SerialName("created")
    val created: Long,

    @SerialName("updated")
    val updated: Long
)
