package ac.kr.smu.endticket.auth

import ac.kr.smu.endTicket.auth.service.TokenService
import ac.kr.smu.endticket.protobuf.AccessToken
import io.grpc.StatusRuntimeException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.params.provider.Arguments
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Stream

object AuthTestParameters{
    @JvmStatic
    fun provideInvalidAccessTokenAndExpectedStatus() = Stream.of(
        Arguments.of(
           Mockito.mock(AccessToken::class.java).also {
               Mockito
                   .`when`(it.token)
                   .thenThrow(Mockito.mock(ExpiredJwtException::class.java))

           }, 401
        ),
        Arguments.of(
            AccessToken.newBuilder().setToken(
                Jwts
                    .builder()
                    .signWith(Keys.hmacShaKeyFor("invalidKeyinvalidKeyinvalidKeyinvalidKeyinvalidKey".toByteArray()))
                    .subject(USER_ID.toString())
                    .compact()
            ).build(), 400
        ),
        Arguments.of(
            Mockito.mock(AccessToken::class.java).also {
                Mockito
                    .`when`(it.token)
                    .thenThrow(Mockito.mock(StatusRuntimeException::class.java))
            }, 500
        )
    )
}