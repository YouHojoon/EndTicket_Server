package ac.kr.smu.endTicket.infra.oAuth.IDToken

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.infra.oAuth.IDToken.exception.IDTokenNotVerifyException
import ac.kr.smu.endTicket.infra.oAuth.IDToken.exception.JWKParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.util.KeyUtils
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Jwk
import io.jsonwebtoken.security.JwkSet
import io.jsonwebtoken.security.Jwks
import kotlinx.coroutines.runBlocking
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.*

/**
 * ID 토큰의 기능을 담당하는 클래스
 * @property clientRegistrationRepository OAuth Client를 저장하고 있는 객체
 * @property redisTemplate 발급받은 공개키들을 저장하기 위한 캐시
 */
@Service
class IDTokenService(
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val redisTemplate: StringRedisTemplate
) {
    /**
     * ID 토큰을 이용해 SNS 사용자 번호를 반환하는 메소드
     * @param socialType ID 토큰을 발급받은 SNS
     * @param idToken ID 토큰
     * @throws IDTokenNotVerifyException ID 토큰 검증 실패 의
     */
    @Throws(IDTokenNotVerifyException::class)
    fun parseSocialUserNumber(socialType: SocialType, idToken: String): Long{
        val (header, payload, _) = parseIDToken(socialType, idToken)
        val key = findPublicKey(socialType, header.kid)

        try {
            verifyIDToken(socialType,idToken, payload, key)
        }catch (e:IllegalArgumentException){
            throw IDTokenNotVerifyException(e.message)
        }

        return payload.sub
    }

    /**
     * ID Token을 검증하는 메소드
     * @param socialType ID 토큰을 발급받은 SNS
     * @param idToken ID 토큰
     * @param payload ID 토큰의 페이로드
     * @param key ID 토큰의 공개키
     * @throws IllegalArgumentException ID 토큰 검증 실패 시
     */
    @Throws(IllegalArgumentException::class)
    private fun verifyIDToken(socialType: SocialType, idToken: String, payload: IDTokenPayLoad, key:PublicKey){
        val provider = clientRegistrationRepository.findByRegistrationId(socialType.name.lowercase())

        require(payload.iss == provider.providerDetails.issuerUri){
            "payload의 iss가 일치하지 않습니다. iss: ${payload.iss}, provider iss: ${provider.providerDetails.issuerUri}"
        }
        require(payload.aud == provider.clientId){
            "aud가 client id와 일치하지 않습니다. aud: ${payload.aud}, provider clientId: ${provider.clientId}"
        }
        require(payload.exp > Instant.now().epochSecond){
            "id 토큰이 만료되었습니다."
        }

        require(Jwts.parser()
            .verifyWith(key)
            .build()
            .isSigned(idToken)
        ){
            "서명 검증에 실패하였습니다."
        }
    }

    /**
     * 외부 SNS 서비스의 공개키를 반환하는 메소드
     * @param socialType 종류
     * @param kid 공개키의 id
     * @return id와 일치하는 공개키 반환
     * @throws IllegalStateException 일치하는 공개키가 없을 시
     */
    @Throws(IllegalStateException::class)
    private fun findPublicKey(socialType: SocialType, kid: String): PublicKey {
        val provider = clientRegistrationRepository.findByRegistrationId(socialType.name.lowercase())
        return runBlocking {
            val jwkSet = getJwkSet(provider)
            val key = jwkSet.filter { jwk: Jwk<*> -> jwk.id == kid }.firstOrNull()?.toKey()
            checkNotNull(key)

            val keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePublic(keyFactory.getKeySpec(key,RSAPublicKeySpec::class.java))
        }
    }

    /**
     * 공개키 목록 조회하기, 캐시에 존재하는 경우 캐시값 반환
     * @param 공개키 목록을 조회할 SNS 서비스
     * @return 조회된 공개키 목록 반환
     * @throws JWKParseException 파싱 실패 시
     */
    @Throws(JWKParseException::class)
    private suspend fun getJwkSet(provider: ClientRegistration): JwkSet{
        val vo = redisTemplate.opsForValue()
        val json = vo.get("${provider.clientName.lowercase()}_jwk_set") ?: WebClient.create()
            .get()
            .uri(provider.providerDetails.jwkSetUri)
            .retrieve()
            .onStatus({ it.isError }) {
                it.createException()
                    .map {
                        JWKParseException(it.getResponseBodyAs(Map::class.java).toString(), it)
                    }
            }
            .awaitBody<String>()
            .also { vo.set("${provider.clientName.lowercase()}_jwk_set", it) }

        return Jwks.setParser().build().parse(json)
    }

    /**
     * SNS의 ID 토큰을 파싱하는 메소드
     * @param socialType ID 토큰을 발급받은 SNS
     * @param token ID 토큰
     * @return 복호화된 ID 토큰의 헤더, 페이로드 그리고 서명
     */
    private fun parseIDToken(socialType: SocialType, token: String): IDToken{
        return when(socialType){
            SocialType.KAKAO -> parseKakaoIDToken(token)
            else ->{
                throw UnsupportedOperationException()
            }
        }
    }

    /**
     * Kakao의 ID 토큰을 파싱하는 메소드
     * @param token: Kakao의 ID 토큰
     * @return 복호화된 ID 토큰의 헤더, 페이로드 그리고 서명
     */
    private fun parseKakaoIDToken(token: String): IDToken{
        val objectMapper = ObjectMapper()
        val (header, payload, signature) = token.split(".")
        val decoder = Base64.getDecoder()

        val decodedPayload = String(decoder.decode(payload))
        val decodedHeader= String(decoder.decode(header))

        return Triple(objectMapper.readValue(decodedHeader, IDTokenHeader::class.java)
            , objectMapper.readValue(decodedPayload, IDTokenPayLoad::class.java), signature)
    }
}

private typealias IDToken = Triple<IDTokenHeader, IDTokenPayLoad,String>