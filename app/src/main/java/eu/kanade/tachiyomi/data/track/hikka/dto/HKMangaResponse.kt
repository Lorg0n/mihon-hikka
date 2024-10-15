package eu.kanade.tachiyomi.data.track.hikka.dto

import eu.kanade.tachiyomi.data.track.hikka.HikkaApi
import eu.kanade.tachiyomi.data.track.hikka.stringToNumber
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HKMangaResponse(
    @SerialName("data_type") val dataType: String,
    @SerialName("title_original") val titleOriginal: String,
    @SerialName("media_type") val mediaType: String,
    @SerialName("title_ua") val titleUa: String? = null,
    @SerialName("title_en") val titleEn: String? = null,
    val chapters: Int? = null,
    val volumes: Int? = null,
    @SerialName("translated_ua") val translatedUa: Boolean,
    val status: String,
    val image: String,
    val year: Int,
    @SerialName("scored_by") val scoredBy: Int,
    val score: Double,
    val slug: String
) {
    fun toTrack(trackId: Long): TrackSearch {
        return TrackSearch.create(trackId).apply {
            remote_id = stringToNumber(this@HKMangaResponse.slug)
            title = this@HKMangaResponse.titleUa ?: this@HKMangaResponse.titleEn ?: this@HKMangaResponse.titleOriginal
            total_chapters = this@HKMangaResponse.chapters?.toLong() ?: 0
            cover_url = this@HKMangaResponse.image
            summary = ""
            score = this@HKMangaResponse.score
            tracking_url = HikkaApi.BASE_URL + "/manga/${this@HKMangaResponse.slug}"
            publishing_status = this@HKMangaResponse.status
            publishing_type = "manga"
            start_date = ""
        }
    }
}
