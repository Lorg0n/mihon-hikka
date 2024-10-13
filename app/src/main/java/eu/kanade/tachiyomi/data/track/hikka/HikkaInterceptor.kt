package eu.kanade.tachiyomi.data.track.hikka

import HikkaAuth
import eu.kanade.tachiyomi.data.track.hikka.dto.HKAuthResponseDto
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.Response
import uy.kohesive.injekt.injectLazy

class HikkaInterceptor (private val hikka: Hikka) : Interceptor {
    private var oauth: HikkaAuth? = hikka.restoreToken()
    private val json: Json by injectLazy()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val currAuth = oauth ?: throw Exception("Not authenticated with Hikka.io")

        if (currAuth.isExpired()) {
            val response = chain.proceed(HikkaApi.refreshTokenRequest(currAuth.secret))
            if (response.isSuccessful) {
                newAuth(HikkaAuth(currAuth.secret, json.decodeFromString<HKAuthResponseDto>(response.body.string())))
            } else {
                response.close()
            }
        }

        val authRequest = originalRequest.newBuilder()
            .addHeader("auth", currAuth.secret)
            .header("Cookie", "auth=${currAuth.secret}")
            .build()

        return chain.proceed(authRequest)
    }

    fun newAuth(oauth: HikkaAuth?) {
        this.oauth = oauth
        hikka.saveToken(oauth)
    }
}
