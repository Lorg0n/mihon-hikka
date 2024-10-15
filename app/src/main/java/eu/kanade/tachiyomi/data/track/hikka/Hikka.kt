package eu.kanade.tachiyomi.data.track.hikka

import android.graphics.Color
import android.util.Log
import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.track.BaseTracker
import eu.kanade.tachiyomi.data.track.DeletableTracker
import eu.kanade.tachiyomi.data.track.hikka.dto.HKOAuth
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tachiyomi.domain.track.model.Track
import tachiyomi.i18n.MR
import uy.kohesive.injekt.injectLazy

class Hikka(id: Long) : BaseTracker(id, "Hikka"), DeletableTracker {

    companion object {
        const val READING = 0L
        const val COMPLETED = 1L
        const val ON_HOLD = 2L
        const val DROPPED = 3L
        const val PLAN_TO_READ = 4L
        const val REREADING = 5L

        private val SCORE_LIST = IntRange(0, 10)
            .map(Int::toString)
            .toImmutableList()
    }

    private val json: Json by injectLazy()

    private val interceptor by lazy { HikkaInterceptor(this) }
    private val api by lazy { HikkaApi(id, client, interceptor) }

    override fun getLogoColor(): Int {
        return Color.rgb(0, 0, 0)
    }

    override fun getLogo(): Int {
        return R.drawable.ic_tracker_hikka
    }

    override fun getStatusList(): List<Long> {
        return listOf(
            READING,
            COMPLETED,
            ON_HOLD,
            DROPPED,
            PLAN_TO_READ,
            REREADING
        )
    }

    override fun getStatus(status: Long): StringResource? = when (status) {
        READING -> MR.strings.reading
        PLAN_TO_READ -> MR.strings.plan_to_read
        COMPLETED -> MR.strings.completed
        ON_HOLD -> MR.strings.on_hold
        DROPPED -> MR.strings.dropped
        REREADING -> MR.strings.repeating
        else -> null
    }

    override fun getReadingStatus(): Long {
        return READING
    }

    override fun getRereadingStatus(): Long {
        return REREADING
    }

    override fun getCompletionStatus(): Long {
        return COMPLETED
    }

    override fun getScoreList(): ImmutableList<String> {
        return SCORE_LIST
    }

    override fun displayScore(track: Track): String {
        return track.score.toInt().toString()
    }

    override suspend fun update(
        track: eu.kanade.tachiyomi.data.database.models.Track,
        didReadChapter: Boolean,
    ): eu.kanade.tachiyomi.data.database.models.Track {
        if (track.status != COMPLETED) {
            if (didReadChapter) {
                if (track.last_chapter_read.toLong() == track.total_chapters && track.total_chapters > 0) {
                    track.status = COMPLETED
                } else if (track.status != REREADING) {
                    track.status = READING
                }
            }
        }
        return track
    }

    override suspend fun bind(
        track: eu.kanade.tachiyomi.data.database.models.Track,
        hasReadChapters: Boolean,
    ): eu.kanade.tachiyomi.data.database.models.Track {
        val remoteTrack = api.getManga(track)
        return if (remoteTrack != null) {
            track.copyPersonalFrom(remoteTrack)
            track.library_id = remoteTrack.library_id

            if (track.status != COMPLETED) {
                val isRereading = track.status == REREADING
                track.status = if (!isRereading && hasReadChapters) READING else track.status
            }

            update(track)
        } else {
            track.status = if (hasReadChapters) READING else PLAN_TO_READ
            track.score = 0.0
            add(track)
        }
    }

    private suspend fun add(track: eu.kanade.tachiyomi.data.database.models.Track): eu.kanade.tachiyomi.data.database.models.Track {
        return api.addUserManga(track)
    }

    override suspend fun search(query: String): List<TrackSearch> {
        return api.searchManga(query)
    }

    override suspend fun refresh(track: eu.kanade.tachiyomi.data.database.models.Track): eu.kanade.tachiyomi.data.database.models.Track {
        val remoteTrack = api.updateUserManga(track)
        track.copyPersonalFrom(remoteTrack)
        track.total_chapters = remoteTrack.total_chapters
        return track
    }

    override suspend fun login(username: String, password: String) = login(password)

    suspend fun login(code: String) {
        try {
            val oauth = HKOAuth(code, System.currentTimeMillis() / 1000 + 30 * 60)
            interceptor.setAuth(oauth)
            val reference =  api.getCurrentUser().reference
            saveCredentials(reference, oauth.secret)
        } catch (e: Throwable) {
            logout()
        }
    }

    override suspend fun delete(track: Track) {
        api.deleteManga(track)
    }

    override fun logout() {
        super.logout()
        trackPreferences.trackToken(this).delete()
        interceptor.setAuth(null)
    }

    fun getIfAuthExpired(): Boolean {
        return trackPreferences.trackAuthExpired(this).get()
    }

    fun setAuthExpired() {
        trackPreferences.trackAuthExpired(this).set(true)
    }

    fun saveOAuth(oAuth: HKOAuth?) {
        trackPreferences.trackToken(this).set(json.encodeToString(oAuth))
    }

    fun loadOAuth(): HKOAuth? {
        return try {
            json.decodeFromString<HKOAuth>(trackPreferences.trackToken(this).get())
        } catch (e: Exception) {
            null
        }
    }
}
