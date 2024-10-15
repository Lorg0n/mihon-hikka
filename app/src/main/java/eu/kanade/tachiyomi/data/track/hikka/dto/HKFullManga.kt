package eu.kanade.tachiyomi.data.track.hikka.dto

import com.google.common.math.Stats
import eu.kanade.tachiyomi.data.track.hikka.HikkaApi
import eu.kanade.tachiyomi.data.track.hikka.stringToNumber
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

@Serializable
data class HKFullManga(
    @SerialName("title_original") val titleOriginal: String,
    @SerialName("synopsis_en") val synopsisEn: String? = null,
    @SerialName("synopsis_ua") val synopsisUa: String? = null,
    val chapters: Int? = null,
    @SerialName("title_en") val titleEn: String? = null,
    @SerialName("title_ua") val titleUa: String? = null,
    val volumes: String?,
    val status: String,
    val image: String,
    val score: Double,
    val slug: String
) {
    fun toTrack(trackId: Long): TrackSearch {
        return TrackSearch.create(trackId).apply {
            remote_id = stringToNumber(this@HKFullManga.slug)
            title = this@HKFullManga.titleUa ?: this@HKFullManga.titleEn ?: this@HKFullManga.titleOriginal
            total_chapters = this@HKFullManga.chapters?.toLong() ?: 0
            cover_url = this@HKFullManga.image
            summary = (this@HKFullManga.synopsisUa ?: this@HKFullManga.synopsisEn).toString()
            score = this@HKFullManga.score
            tracking_url = HikkaApi.BASE_URL + "/manga/${this@HKFullManga.slug}"
            publishing_status = this@HKFullManga.status
        }
    }
}
