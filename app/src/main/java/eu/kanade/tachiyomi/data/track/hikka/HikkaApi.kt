package eu.kanade.tachiyomi.data.track.hikka

import eu.kanade.tachiyomi.data.database.models.Track
import eu.kanade.tachiyomi.data.track.hikka.dto.Context
import eu.kanade.tachiyomi.data.track.mangaupdates.dto.ListItem
import eu.kanade.tachiyomi.data.track.mangaupdates.dto.Rating
import eu.kanade.tachiyomi.data.track.hikka.dto.Record
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.parseAs
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import logcat.LogPriority
import logcat.logcat
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import tachiyomi.core.common.util.system.logcat
import uy.kohesive.injekt.injectLazy
import java.util.UUID
import tachiyomi.domain.track.model.Track as DomainTrack

class HikkaApi(
    interceptor: HikkaInterceptor,
    private val client: OkHttpClient,
) {
    private val json: Json by injectLazy()

    private val baseUrl = "https://api.hikka.io"
    private val contentType = "application/json".toMediaTypeOrNull()

    private var authCode = ""

    suspend fun getSeriesListItem(track: Track): Pair<ListItem, Rating?> {
        val args = track.tracking_url.split("/")
        val slug = args.last()

        val rating = getSeriesRating(track)
        logcat.logcat("MSG", LogPriority.ERROR, {"Score: " + rating?.rating.toString()})

        val request = Request.Builder()
            .url("$baseUrl/manga/${slug}")
            .get()
            .addHeader("accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()

        val listItem = with(json) {
            client.newCall(request)
                .awaitSuccess()
                .parseAs<ListItem>()
        }

        //val rating = getSeriesRating(track)
        //logcat.logcat("MSG", LogPriority.ERROR, {rating?.rating.toString()})
        return listItem to rating
    }

    suspend fun addSeriesToList(track: Track, hasReadChapters: Boolean) {
//        val status = if (hasReadChapters) READING_LIST else WISH_LIST
//        val body = buildJsonArray {
//            addJsonObject {
//                putJsonObject("series") {
//                    put("id", track.remote_id)
//                }
//                put("list_id", status)
//            }
//        }
//        authCode.newCall(
//            POST(
//                url = "$baseUrl/v1/lists/series",
//                body = body.toString().toRequestBody(contentType),
//            ),
//        )
//            .awaitSuccess()
//            .let {
//                if (it.code == 200) {
//                    track.status = status
//                    track.last_chapter_read = 1.0
//                }
//            }
    }

    suspend fun updateSeriesListItem(track: Track) {
//        val body = buildJsonArray {
//            addJsonObject {
//                putJsonObject("series") {
//                    put("id", track.remote_id)
//                }
//                put("list_id", track.status)
//                putJsonObject("status") {
//                    put("chapter", track.last_chapter_read.toInt())
//                }
//            }
//        }
//        authCode.newCall(
//            POST(
//                url = "$baseUrl/v1/lists/series/update",
//                body = body.toString().toRequestBody(contentType),
//            ),
//        )
//            .awaitSuccess()
//
//        updateSeriesRating(track)
    }

    suspend fun deleteSeriesFromList(track: DomainTrack) {
//        val body = buildJsonArray {
//            add(track.remoteId)
//        }
//        authCode.newCall(
//            POST(
//                url = "$baseUrl/v1/lists/series/delete",
//                body = body.toString().toRequestBody(contentType),
//            ),
//        )
//            .awaitSuccess()

        val args = track.remoteUrl.split("/")
        val mangaSlug = args.last()

        val request = Request.Builder()
            .url("$baseUrl/read/manga/${mangaSlug}")
            .delete()
            .addHeader("accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()

        // client.newCall(request).awaitSuccess()
    }

    private suspend fun getSeriesRating(track: Track): Rating? {
        val args = track.tracking_url.split("/")
        val slug = args.last()

        logcat.logcat("TEST", LogPriority.WARN, {"$baseUrl/read/manga/${slug}"})

        val request = Request.Builder()
            .url("$baseUrl/read/manga/${slug}")
            .get()
            .addHeader("Cookie", "auth=$authCode")
            .addHeader("auth", authCode)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            return null
        }

        val responseBody = response.body?.string()
        return responseBody?.let {
            val jsonElement = json.parseToJsonElement(it)
            Rating(jsonElement.jsonObject["score"].toString().toDouble())
        }
    }

    private suspend fun updateSeriesRating(track: Track) {
//        val body = buildJsonObject {
//            put("rating", track.score)
//        }
//        authCode.newCall(
//            PUT(
//                url = "$baseUrl/v1/series/${track.remote_id}/rating",
//                body = body.toString().toRequestBody(contentType),
//            ),
//        )
//            .awaitSuccess()
        val body = buildJsonObject {
            put("score", track.score)
        }
        val args = track.tracking_url.split("/")
        val mangaSlug = args.last()

        val request = Request.Builder()
            .url("$baseUrl/read/manga/${mangaSlug}")
            .put(body.toString().toRequestBody(contentType))
            .addHeader("accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).awaitSuccess()
    }

    suspend fun search(query: String): List<Record> {
        val body = buildJsonObject {
            put("query", query)
            put(
                "sort",
                buildJsonArray {
                    add("score:desc")
                    add("scored_by:desc")
                }
            )
        }

        val request = Request.Builder()
            .url("$baseUrl/manga?page=1&size=15")
            .post(Json.encodeToString(body).toRequestBody("application/json; charset=utf-8".toMediaType()))
            .addHeader("accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).awaitSuccess()
        val json = Json { ignoreUnknownKeys = true }

        return json.decodeFromString<JsonObject>(response.body!!.string())
            .get("list")!!
            .jsonArray
            .map { json.decodeFromJsonElement<Record>(it) }
    }

    suspend fun authenticate(auth: String): Context? {
        val headers = Headers.Builder()
            .add("Cookie", "auth=$auth")
            .add("auth", auth)
            .build()

        val request = Request.Builder()
            .url("$baseUrl/user/me")
            .get()
            .headers(headers)
            .build()

        return with(json) {
            client.newCall(request).awaitSuccess().parseAs<Context>().let { context ->
                try {
                    authCode = auth
                    logcat.logcat("TEST", LogPriority.INFO, {"Auth Code: " + authCode + " / auth: " + auth})
                    context
                } catch (e: Exception) {
                    logcat(LogPriority.ERROR, e)
                    null
                }
            }
        }
    }
}
