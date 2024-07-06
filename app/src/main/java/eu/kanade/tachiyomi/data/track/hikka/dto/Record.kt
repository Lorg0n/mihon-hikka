package eu.kanade.tachiyomi.data.track.hikka.dto

import eu.kanade.tachiyomi.data.track.model.TrackSearch
import eu.kanade.tachiyomi.util.lang.htmlDecode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Record(
    @SerialName("data_type")
    val dataType: String? = null,
    @SerialName("title_original")
    val titleOriginal: String? = null,
    @SerialName("media_type")
    val mediaType: String? = null,
    @SerialName("title_ua")
    val titleUa: String? = null,
    @SerialName("title_en")
    val titleEn: String? = null,
    val chapters: Int? = null,
    val volumes: Int? = null,
    @SerialName("translated_ua")
    val translatedUa: Boolean? = null,
    val status: String? = null,
    val image: String? = null,
    val year: Int? = null,
    @SerialName("scored_by")
    val scoredBy: Int? = null,
    val score: Double? = null,
    val slug: String? = null,
    val read: List<String>? = null,
    val tracking_url: String = "https://hikka.io/manga/${slug}",
)

fun Record.toTrackSearch(id: Long): TrackSearch {
    return TrackSearch.create(id).apply {
        remote_id = this@toTrackSearch.slug?.hashCode()?.toLong() ?: 0L
        title = this@toTrackSearch.titleOriginal?.htmlDecode() ?: ""
        total_chapters = (this@toTrackSearch.chapters ?: 0).toLong()
        cover_url = this@toTrackSearch.image ?: ""
        summary = ""
        tracking_url = this@toTrackSearch.tracking_url
        publishing_status = this@toTrackSearch.status ?: ""
        publishing_type = this@toTrackSearch.mediaType ?: ""
        start_date = this@toTrackSearch.year?.toString() ?: ""
    }
}
