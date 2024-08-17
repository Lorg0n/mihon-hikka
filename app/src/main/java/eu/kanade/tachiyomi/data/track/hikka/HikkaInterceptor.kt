package eu.kanade.tachiyomi.data.track.hikka

import eu.kanade.tachiyomi.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class HikkaInterceptor(private val kavita: Hikka) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        return chain.proceed(originalRequest);
    }
}
