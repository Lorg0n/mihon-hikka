package eu.kanade.tachiyomi.data.track.hikka

import android.graphics.Color
import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.database.models.Track
import eu.kanade.tachiyomi.data.track.BaseTracker
import eu.kanade.tachiyomi.data.track.DeletableTracker
import eu.kanade.tachiyomi.data.track.hikka.dto.toTrackSearch
import eu.kanade.tachiyomi.data.track.mangaupdates.dto.ListItem
import eu.kanade.tachiyomi.data.track.mangaupdates.dto.Rating
import eu.kanade.tachiyomi.data.track.mangaupdates.dto.copyTo
import eu.kanade.tachiyomi.data.track.mangaupdates.dto.toTrackSearch
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.SerialName
import tachiyomi.i18n.MR
import tachiyomi.domain.track.model.Track as DomainTrack

class Hikka(id: Long) : BaseTracker(id, "Hikka"), DeletableTracker {

    companion object {
        const val READING = 0L // Список для читання
        const val PLANNED = 1L // Список бажань
        const val COMPLETED = 2L // Список завершених
        const val DROPPED = 3L // Список незавершених
        const val ON_HOLD = 4L // Список на паузі

        private val SCORE_LIST = (0..10)
            .flatMap { decimal ->
                when (decimal) {
                    0 -> listOf("-")
                    10 -> listOf("10.0")
                    else -> (0..9).map { fraction ->
                        "$decimal.$fraction"
                    }
                }
            }
            .toImmutableList()
    }

    private val interceptor by lazy { HikkaInterceptor(this) }

    private val api by lazy { HikkaApi(interceptor, client) }

    override fun getLogo(): Int = R.drawable.ic_tracker_hikka

    override fun getLogoColor(): Int = Color.rgb(0, 0, 0)

    override fun getStatusList(): List<Long> {
        return listOf(READING, COMPLETED, ON_HOLD, DROPPED, PLANNED)
    }

    override fun getStatus(status: Long): StringResource? = when (status) {
        READING -> MR.strings.reading_list
        PLANNED -> MR.strings.wish_list
        COMPLETED -> MR.strings.complete_list
        ON_HOLD -> MR.strings.on_hold_list
        DROPPED -> MR.strings.unfinished_list
        else -> null
    }

    override fun getReadingStatus(): Long = READING

    override fun getRereadingStatus(): Long = -1

    override fun getCompletionStatus(): Long = COMPLETED

    override fun getScoreList(): ImmutableList<String> = SCORE_LIST

    override fun indexToScore(index: Int): Double = if (index == 0) 0.0 else SCORE_LIST[index].toDouble()

    override fun displayScore(track: DomainTrack): String = track.score.toString()

    override suspend fun update(track: Track, didReadChapter: Boolean): Track {
        if (track.status != COMPLETED && didReadChapter) {
            track.status = READING
        }
        api.updateSeriesListItem(track)
        return track
    }

    override suspend fun delete(track: DomainTrack) {
        api.deleteSeriesFromList(track)
    }

    override suspend fun bind(track: Track, hasReadChapters: Boolean): Track {
        return try {
            val (series, rating) = api.getSeriesListItem(track)
            track.copyFrom(series, rating)
        } catch (e: Exception) {
            track.score = 0.0
            api.addSeriesToList(track, hasReadChapters)
            track
        }
    }

    override suspend fun search(query: String): List<TrackSearch> {
        return api.search(query)
            .map {
                it.toTrackSearch(id)
            }
    }

    override suspend fun refresh(track: Track): Track {
        val (series, rating) = api.getSeriesListItem(track)
        return track.copyFrom(series, rating)
    }

    private fun Track.copyFrom(item: ListItem, rating: Rating?): Track = apply {
        item.copyTo(this)
        score = rating?.rating ?: 0.0
    }

    override suspend fun login(username: String, password: String) {
        val authenticated = api.authenticate(password) ?: throw Throwable("Unable to login")
        saveCredentials(authenticated.username.toString(), password)
        interceptor.newAuth(password)
    }

    fun restoreSession(): String? {
        return trackPreferences.trackPassword(this).get().ifBlank { null }
    }
}
