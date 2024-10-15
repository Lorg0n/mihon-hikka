package eu.kanade.tachiyomi.data.track.hikka

import android.graphics.Color
import android.util.Log
import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.track.BaseTracker
import eu.kanade.tachiyomi.data.track.DeletableTracker
import eu.kanade.tachiyomi.data.track.hikka.dto.HKOAuth
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import eu.kanade.tachiyomi.data.track.myanimelist.MyAnimeList
import eu.kanade.tachiyomi.data.track.myanimelist.MyAnimeList.Companion
import eu.kanade.tachiyomi.data.track.myanimelist.MyAnimeListApi
import eu.kanade.tachiyomi.data.track.myanimelist.MyAnimeListInterceptor
import eu.kanade.tachiyomi.data.track.myanimelist.dto.MALOAuth
import eu.kanade.tachiyomi.data.track.shikimori.Shikimori
import eu.kanade.tachiyomi.data.track.shikimori.ShikimoriApi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tachiyomi.domain.track.model.Track
import tachiyomi.i18n.MR
import uy.kohesive.injekt.injectLazy

class Hikka(id: Long) : BaseTracker(id, "Hikka"), DeletableTracker {

    companion object {
        const val READING = 1L
        const val COMPLETED = 2L
        const val ON_HOLD = 3L
        const val DROPPED = 4L
        const val PLAN_TO_READ = 6L
        const val REREADING = 7L

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
        TODO("Not yet implemented")
    }

    override suspend fun bind(
        track: eu.kanade.tachiyomi.data.database.models.Track,
        hasReadChapters: Boolean,
    ): eu.kanade.tachiyomi.data.database.models.Track {
        TODO("NOT FIND BIND")
    }

    override suspend fun search(query: String): List<TrackSearch> {
        return api.searchManga(query)
    }

    override suspend fun refresh(track: eu.kanade.tachiyomi.data.database.models.Track): eu.kanade.tachiyomi.data.database.models.Track {
        TODO("Not yet implemented")
    }

    override suspend fun login(username: String, password: String) = login(password)

    suspend fun login(code: String) {
        try {
            Log.println(Log.WARN, "login", "Log In")
            // val tokenInfo = api.getTokenInfo()
            // Log.println(Log.WARN, "login", "Token Info: " + json.encodeToString(tokenInfo))
            val oauth = HKOAuth(code, System.currentTimeMillis() / 1000 + 30 * 60)
            Log.println(Log.WARN, "login", "Create OAUTH" + json.encodeToString(oauth))

            interceptor.setAuth(oauth)
            Log.println(Log.WARN, "login", "interceptor Set Auth: " + json.encodeToString(oauth))
            val reference =  api.getCurrentUser()
            Log.println(Log.WARN, "login", "Reference: $reference")
            saveCredentials(reference, oauth.secret)
            Log.println(Log.WARN, "login", "Secret: $oauth.secret")
        } catch (e: Throwable) {
            Log.println(Log.WARN, "login", "Error: Log Out")
            logout()
        }
    }

    override suspend fun delete(track: Track) {
        TODO("Not yet implemented")
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
