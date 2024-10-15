package eu.kanade.tachiyomi.data.track.hikka

import android.net.Uri
import androidx.core.net.toUri
import eu.kanade.tachiyomi.data.track.hikka.dto.HKMangaList
import eu.kanade.tachiyomi.data.track.hikka.dto.HKOAuth
import eu.kanade.tachiyomi.data.track.hikka.dto.HKTokenInfo
import eu.kanade.tachiyomi.data.track.hikka.dto.HKUser
import eu.kanade.tachiyomi.data.track.model.TrackSearch
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.jsonMime
import eu.kanade.tachiyomi.network.parseAs
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import tachiyomi.core.common.util.lang.withIOContext
import uy.kohesive.injekt.injectLazy

class HikkaApi(
    private val trackId: Long,
    private val client: OkHttpClient,
    interceptor: HikkaInterceptor,
) {
    suspend fun getCurrentUser(): String {
        return withIOContext {
            val request = Request.Builder()
                .url("${BASE_API_URL}/user/me")
                .get()
                .build()
            with(json) {
                authClient.newCall(request)
                    .awaitSuccess()
                    .parseAs<HKUser>()
                    .reference
            }
        }
    }

    suspend fun getTokenInfo(): HKTokenInfo {
        return withIOContext {
            val request = Request.Builder()
                .url("${BASE_API_URL}/auth/token/info")
                .get()
                .build()
            with(json) {
                authClient.newCall(request)
                    .awaitSuccess()
                    .parseAs<HKTokenInfo>()
            }
        }
    }

    suspend fun searchManga(query: String): List<TrackSearch> {
        return withIOContext {
            val url = "$BASE_API_URL/manga".toUri().buildUpon()
                .appendQueryParameter("page", "1")
                .appendQueryParameter("size", "50")
                .build()

            val payload = buildJsonObject {
                put("media_type", buildJsonArray { })
                put("status", buildJsonArray { })
                put("only_translated", false)
                put("magazines", buildJsonArray { })
                put("genres", buildJsonArray { })
                put("score", buildJsonArray {
                    add(0)
                    add(10)
                })
                put("query", query)
                put("sort", buildJsonArray {
                    add("score:asc")
                    add("scored_by:asc")
                })
            }

            with(json) {
                authClient.newCall(POST(url.toString(), body=payload.toString().toRequestBody(jsonMime)))
                    .awaitSuccess()
                    .parseAs<HKMangaList>()
                    .list
                    .map { it.toTrack(trackId) }
            }
        }
    }

    private val json: Json by injectLazy()
    private val authClient = client.newBuilder().addInterceptor(interceptor).build()

    companion object {
        const val BASE_API_URL = "https://hikka.io/api"
        const val BASE_URL = "https://hikka.io"
        private const val SCOPE = "readlist,read:user-details"
        private const val REFERENCE = "49eda83d-baa6-45f8-9936-b2a41d944da4"

        fun authUrl(): Uri = "$BASE_URL/oauth".toUri().buildUpon()
            .appendQueryParameter("reference", REFERENCE)
            .appendQueryParameter("scope", SCOPE)
            .build()

        fun refreshTokenRequest(oauth: HKOAuth): Request {
            val headers = Headers.Builder()
                .add("auth", oauth.secret)
                .add("Cookie", "auth=${oauth.secret}")
                .build()

            return GET("$BASE_API_URL/auth/token/info", headers = headers)
        }
    }
}
