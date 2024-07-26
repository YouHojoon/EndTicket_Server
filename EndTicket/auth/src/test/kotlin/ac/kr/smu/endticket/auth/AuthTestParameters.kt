package ac.kr.smu.endticket.auth

import ac.kr.smu.endticket.auth.domain.model.SocialType
import ac.kr.smu.endticket.auth.ui.response.TokenResponse
import ac.kr.smu.endticket.protobuf.AccessToken
import io.grpc.StatusRuntimeException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.params.provider.Arguments
import org.mockito.Mockito
import java.util.stream.Stream

object AuthTestParameters {
    const val AUTHORIZATION_CODE = "1"
    const val SOCIAL_USER_NUMBER = "1"
    const val USER_ID = 1L
    const val ACCESS_TOKEN = "a"
    const val REFRESH_TOKEN = "r"
    const val ID_TOKEN = "i"
    const val PATH = "ac.kr.smu.endticket.auth.AuthTestParameters"
    val TOKEN_RESPONSE =
        TokenResponse(
            ACCESS_TOKEN,
            REFRESH_TOKEN,
        )
    val SOCIAL_TYPE = SocialType.KAKAO

    @JvmStatic
    fun provideInvalidAccessTokenAndExpectedStatus() =
        Stream.of(
            Arguments.of(
                Mockito.mock(AccessToken::class.java).also {
                    Mockito
                        .`when`(it.token)
                        .thenThrow(Mockito.mock(ExpiredJwtException::class.java))
                },
                401,
            ),
            Arguments.of(
                AccessToken
                    .newBuilder()
                    .setToken(
                        Jwts
                            .builder()
                            .signWith(Keys.hmacShaKeyFor("invalidKeyinvalidKeyinvalidKeyinvalidKeyinvalidKey".toByteArray()))
                            .subject(USER_ID.toString())
                            .compact(),
                    ).build(),
                400,
            ),
            Arguments.of(
                Mockito.mock(AccessToken::class.java).also {
                    Mockito
                        .`when`(it.token)
                        .thenThrow(Mockito.mock(StatusRuntimeException::class.java))
                },
                500,
            ),
        )

    @JvmStatic
    fun provideInvalidRefreshToken() =
        Stream.of(
            Arguments.of("aaa"),
            Arguments.of(null),
        )
}
