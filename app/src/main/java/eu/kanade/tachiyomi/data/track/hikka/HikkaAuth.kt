import kotlinx.serialization.Serializable
import eu.kanade.tachiyomi.data.track.hikka.dto.HKAuthResponseDto

@Serializable
class HikkaAuth(val secret: String, val token: HKAuthResponseDto) {
    fun isExpired(): Boolean {
        return (System.currentTimeMillis() / 1000) > (token.created + token.expiration - 3600)
    }
}
