package eu.kanade.tachiyomi.data.track.hikka

import android.graphics.Color
import dev.icerock.moko.resources.StringResource
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.track.BaseTracker
import eu.kanade.tachiyomi.data.track.DeletableTracker
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import kotlinx.collections.immutable.ImmutableList
import tachiyomi.domain.track.model.Track

class Hikka(id: Long) : BaseTracker(id, "Hikka"), DeletableTracker {
    override fun getLogoColor() = Color.rgb(0, 0, 0);

    override fun getLogo() = R.drawable.ic_tracker_hikka;

    override fun getStatusList(): List<Long> {
        TODO("Not yet implemented")
    }

    override fun getStatus(status: Long): StringResource? {
        TODO("Not yet implemented")
    }

    override fun getReadingStatus(): Long {
        TODO("Not yet implemented")
    }

    override fun getRereadingStatus(): Long {
        TODO("Not yet implemented")
    }

    override fun getCompletionStatus(): Long {
        TODO("Not yet implemented")
    }

    override fun getScoreList(): ImmutableList<String> {
        TODO("Not yet implemented")
    }

    override fun displayScore(track: Track): String {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override suspend fun search(query: String): List<TrackSearch> {
        TODO("Not yet implemented")
    }

    override suspend fun refresh(track: eu.kanade.tachiyomi.data.database.models.Track): eu.kanade.tachiyomi.data.database.models.Track {
        TODO("Not yet implemented")
    }

    override suspend fun login(username: String, password: String) = login(password)

    suspend fun login(code: String) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(track: Track) {
        TODO("Not yet implemented")
    }

}
