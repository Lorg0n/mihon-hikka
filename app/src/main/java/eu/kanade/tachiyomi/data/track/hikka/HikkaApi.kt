package eu.kanade.tachiyomi.data.track.hikka

import android.net.Uri
import androidx.core.net.toUri
import eu.kanade.tachiyomi.data.track.hikka.dto.HKCurrentUser
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.parseAs
import kotlinx.serialization.json.Json
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import uy.kohesive.injekt.injectLazy

class HikkaApi (
    private val client: OkHttpClient,
    interceptor: HikkaInterceptor,
) {

    private val json: Json by injectLazy()
    private val authClient = client.newBuilder().addInterceptor(interceptor).build()

    suspend fun getCurrentUser(): String {
        return with(json) {
            val request = Request.Builder()
                .url("${BASE_URL}api/user/me")
                .build()

            authClient.newCall(request).awaitSuccess()
                .parseAs<HKCurrentUser>()
                .reference
        }
    }

    companion object {
        private const val CLIENT_REFERENCE = "49eda83d-baa6-45f8-9936-b2a41d944da4"
        private const val BASE_URL = "https://hikka.io/"

        fun authUrl(): Uri = "${BASE_URL}oauth".toUri().buildUpon()
            .appendQueryParameter("reference", CLIENT_REFERENCE)
            .appendQueryParameter("scope", "readlist")
            .build()

        fun refreshTokenRequest(secret: String) = GET(
            "${BASE_URL}api/user/me",
            headers = Headers.Builder()
                .add("auth", secret)
                .add("Cookie", "auth=${secret}")
                .build(),
        )
    }
}
