package eu.kanade.tachiyomi.data.track.hikka

import android.net.Uri
import androidx.core.net.toUri
import eu.kanade.tachiyomi.data.track.shikimori.ShikimoriInterceptor
import okhttp3.OkHttpClient

class HikkaApi (
    private val client: OkHttpClient,
    interceptor: ShikimoriInterceptor,
) {
    companion object {
        private const val CLIENT_REFERENCE = "49eda83d-baa6-45f8-9936-b2a41d944da4"
        private const val BASE_URL = "https://hikka.io/"

        fun authUrl(): Uri = "${BASE_URL}oauth".toUri().buildUpon()
            .appendQueryParameter("reference", CLIENT_REFERENCE)
            .appendQueryParameter("scope", "readlist")
            .build()
    }
}
