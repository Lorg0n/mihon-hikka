package eu.kanade.tachiyomi.data.track.hikka.dto

import eu.kanade.tachiyomi.data.track.hikka.HikkaApi
import eu.kanade.tachiyomi.data.track.hikka.stringToNumber
import eu.kanade.tachiyomi.data.track.hikka.toTrackStatus
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import kotlinx.serialization.Serializable

@Serializable
data class HKReadResponse(
    val reference: String,
    val note: String,
    val updated: Long,
    val created: Long,
    val status: String,
    val chapters: Int,
    val volumes: Int,
    val rereads: Int,
    val score: Int,
    val content: HKMangaResponse
) {
    fun toTrack(trackId: Long): TrackSearch {
        return TrackSearch.create(trackId).apply {
            title = this@HKReadResponse.content.titleUa ?: this@HKReadResponse.content.titleEn ?: this@HKReadResponse.content.titleOriginal
            remote_id = stringToNumber(this@HKReadResponse.content.slug)
            total_chapters = this@HKReadResponse.content.chapters?.toLong() ?: 0
            library_id = stringToNumber(this@HKReadResponse.content.slug)
            last_chapter_read = this@HKReadResponse.chapters.toDouble()
            score = this@HKReadResponse.score.toDouble()
            status = toTrackStatus(this@HKReadResponse.status)
            tracking_url = HikkaApi.BASE_URL + "/manga/${this@HKReadResponse.content.slug}"
        }
    }
}
