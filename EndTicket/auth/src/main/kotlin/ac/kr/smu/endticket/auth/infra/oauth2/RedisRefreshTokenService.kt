package ac.kr.smu.endticket.auth.infra.oauth2

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import org.springframework.stereotype.Service
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * Redis에 Refresh 토큰을 저장하는 서비스
 * @property redisTemplate Redis 저장을 위한 객체
 */
@Service
class RedisRefreshTokenService(
    private val redisTemplate: RedisTemplate<String, Any>,
) {
    private val log = LoggerFactory.getLogger(RedisRefreshTokenService::class.java)

    private companion object {
        private const val USER_REFRESH_TOKEN_PREFIX = "refresh_token::"
    }

    /**
     * Refresh 토큰과 인증 객체를 저장
     * @param token Refresh 토큰
     * @param record 인증 객체
     */
    fun save(
        token: OAuth2RefreshToken,
        record: OAuth2AuthorizationRecord,
    ) {
        val ops = redisTemplate.opsForValue()
        val expires = ChronoUnit.SECONDS.between(token.issuedAt, token.expiresAt)

        // 토큰 정보 저장
        ops.set(
            token.tokenValue,
            record,
            expires,
            TimeUnit.SECONDS,
        )
        // 사용자 정보 저장
        ops.set(
            "$USER_REFRESH_TOKEN_PREFIX${record.principalName}",
            token.tokenValue,
            expires,
            TimeUnit.SECONDS,
        )
    }

    /**
     * 인증 객체 조회
     * @param token Refresh 토큰
     * @return 조회된 인증 객체, 없다면 null을 반환한다.
     */
    fun find(token: String): OAuth2AuthorizationRecord? {
        val ops = redisTemplate.opsForValue()

        // null일 경우 refresh 토큰 만료 or 비정상적 토큰
        val record = ops.get(token)?.let { it as OAuth2AuthorizationRecord } ?: return null

        // null일 경우 사용자 만료 혹은 재인증 필요
        val userRefreshToken =
            ops.getAndDelete("$USER_REFRESH_TOKEN_PREFIX${record.principalName}")?.toString() ?: return null

        // 인증 성공
        if (userRefreshToken == token) {
            return record
        } else {
            // 토큰 탈취 가능성
            log.warn("비정상적인 토큰 갱신 요청 : {token : $token, userId: ${record.principalName}}")
            return null
        }
    }

    /**
     * 토큰의 정보 삭제
     * @param token Refresh 토큰
     */
    fun remove(token: String) {
        redisTemplate.opsForValue().getAndDelete(token)?.let {
            removeByUserId(it.toString())
        }
    }

    /**
     * 사용자의 정보 삭제
     * @param userId 사용자 id
     */
    fun removeByUserId(userId: String) = redisTemplate.delete("$USER_REFRESH_TOKEN_PREFIX$userId")
}
