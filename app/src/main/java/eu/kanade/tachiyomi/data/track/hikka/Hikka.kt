package eu.kanade.tachiyomi.data.track.hikka

import HikkaAuth
import android.graphics.Color
import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.track.BaseTracker
import eu.kanade.tachiyomi.data.track.DeletableTracker
import eu.kanade.tachiyomi.data.track.hikka.dto.HKAuthResponseDto
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.parseAs
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Request
import tachiyomi.domain.track.model.Track
import tachiyomi.i18n.MR
import uy.kohesive.injekt.injectLazy

class Hikka(id: Long) : BaseTracker(id, "Hikka"), DeletableTracker {

    companion object {
        const val READING = 1L
        const val COMPLETED = 2L
        const val ON_HOLD = 3L
        const val DROPPED = 4L
        const val PLAN_TO_READ = 5L
        const val REREADING = 6L

        private val SCORE_LIST = IntRange(0, 10)
            .map(Int::toString)
            .toImmutableList()
    }

    private val interceptor by lazy { HikkaInterceptor(this) }
    val api by lazy { HikkaApi(client, interceptor) }
    private val json: Json by injectLazy()

    override fun getLogoColor() = Color.rgb(0, 0, 0);

    override fun getLogo() = R.drawable.ic_tracker_hikka;

    override fun getStatusList(): List<Long> {
        return listOf(READING, COMPLETED, ON_HOLD, DROPPED, PLAN_TO_READ)
    }

    override fun getStatus(status: Long): StringResource? {
        return when (status) {
            READING -> MR.strings.reading
            PLAN_TO_READ -> MR.strings.plan_to_read
            COMPLETED -> MR.strings.completed
            ON_HOLD -> MR.strings.on_hold
            DROPPED -> MR.strings.dropped
            REREADING -> MR.strings.repeating
            else -> null
        }
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
        return eu.kanade.tachiyomi.data.database.models.Track.create(0)
    }

    override suspend fun bind(
        track: eu.kanade.tachiyomi.data.database.models.Track,
        hasReadChapters: Boolean,
    ): eu.kanade.tachiyomi.data.database.models.Track {
        return eu.kanade.tachiyomi.data.database.models.Track.create(0)
    }

    override suspend fun search(query: String): List<TrackSearch> {
        return listOf()
    }

    override suspend fun refresh(track: eu.kanade.tachiyomi.data.database.models.Track): eu.kanade.tachiyomi.data.database.models.Track {
        return eu.kanade.tachiyomi.data.database.models.Track.create(0)
    }

    override suspend fun login(username: String, password: String) = login(password)

    suspend fun login(secret: String) {
        try {
            val token = with(Json) {
                val request = Request.Builder()
                    .url("https://api.hikka.io/auth/token/info")
                    .header("auth", secret)
                    .header("Cookie", "auth=${secret}")
                    .build()

                client.newCall(request).awaitSuccess()
                    .parseAs<HKAuthResponseDto>()
            }
            val oauth = HikkaAuth(secret, token);
            interceptor.newAuth(oauth)
            val user = api.getCurrentUser()
            saveCredentials(user, oauth.secret)
        } catch (e: Throwable) {
            logout()
        }
    }

    fun saveToken(oauth: HikkaAuth?) {
        trackPreferences.trackToken(this).set(json.encodeToString(oauth))
    }

    fun restoreToken(): HikkaAuth? {
        return try {
            json.decodeFromString<HikkaAuth>(trackPreferences.trackToken(this).get())
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun delete(track: Track) {
        
    }

    override fun logout() {
        super.logout()
        trackPreferences.trackToken(this).delete()
        interceptor.newAuth(null)
    }
}
