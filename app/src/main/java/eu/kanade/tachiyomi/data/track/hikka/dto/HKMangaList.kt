package eu.kanade.tachiyomi.data.track.hikka.dto

import eu.kanade.tachiyomi.data.track.hikka.HikkaApi
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class HKMangaList(
    val pagination: HKPagination,
    val list: List<HKManga>
)

@Serializable
data class HKManga(
    val data_type: String,
    val title_original: String,
    val media_type: String,
    val title_ua: String? = null,
    val title_en: String? = null,
    val chapters: Int? = null,
    val volumes: Int? = null,
    val translated_ua: Boolean,
    val status: String,
    val image: String,
    val year: Int,
    val scored_by: Int,
    val score: Double,
    val slug: String,
) {
    fun toTrack(trackId: Long): TrackSearch {
        return TrackSearch.create(trackId).apply {
            remote_id = UUID.nameUUIDFromBytes(this@HKManga.slug.toByteArray()).mostSignificantBits and Long.MAX_VALUE
            title = this@HKManga.title_ua ?: this@HKManga.title_en ?: this@HKManga.title_original
            total_chapters = this@HKManga.chapters?.toLong() ?: 0
            cover_url = this@HKManga.image
            summary = ""
            score = this@HKManga.score
            tracking_url = HikkaApi.BASE_URL + "/manga/${this@HKManga.slug}"
            publishing_status = this@HKManga.status
            publishing_type = "manga"
            start_date = ""
        }
    }
}
