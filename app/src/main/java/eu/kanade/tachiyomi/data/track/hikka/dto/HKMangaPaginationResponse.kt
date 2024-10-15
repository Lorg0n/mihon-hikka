package eu.kanade.tachiyomi.data.track.hikka.dto

import kotlinx.serialization.Serializable

@Serializable
data class HKMangaPaginationResponse(
    val pagination: HKPaginationResponse,
    val list: List<HKMangaResponse>
)
