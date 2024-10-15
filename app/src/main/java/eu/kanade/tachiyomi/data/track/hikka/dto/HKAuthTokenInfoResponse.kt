package eu.kanade.tachiyomi.data.track.hikka.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HKAuthTokenInfoResponse(
    @SerialName("reference")
    val reference: String,

    @SerialName("created")
    val created: Long,

    @SerialName("client")
    val client: HKClientResponse,

    @SerialName("scope")
    val scope: List<String>,

    @SerialName("expiration")
    val expiration: Long,

    @SerialName("used")
    val used: Long
)
