package eu.kanade.tachiyomi.data.track.hikka.dto

import android.util.Log
import eu.kanade.tachiyomi.data.track.hikka.HikkaApi
import eu.kanade.tachiyomi.data.track.hikka.stringToNumber
import eu.kanade.tachiyomi.data.track.hikka.toTrackStatus
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import kotlinx.serialization.Serializable

@Serializable
data class HKReadData(
    val reference: String,
    val note: String,
    val updated: Long,
    val created: Long,
    val status: String,
    val chapters: Int,
    val volumes: Int,
    val rereads: Int,
    val score: Int,
    val content: HKManga
) {
    fun toTrack(trackId: Long): TrackSearch {
        Log.println(Log.WARN, "ReadData", this@HKReadData.status + " / " + toTrackStatus(this@HKReadData.status))
        return TrackSearch.create(trackId).apply {
            title = this@HKReadData.content.title_ua ?: this@HKReadData.content.title_en ?: this@HKReadData.content.title_original
            remote_id = stringToNumber(this@HKReadData.content.slug)
            total_chapters = this@HKReadData.content.chapters?.toLong() ?: 0
            library_id = stringToNumber(this@HKReadData.content.slug)
            last_chapter_read = this@HKReadData.chapters.toDouble()
            score = this@HKReadData.score.toDouble()
            status = toTrackStatus(this@HKReadData.status)
            tracking_url = HikkaApi.BASE_URL + "/manga/${this@HKReadData.content.slug}"
        }
    }
}
