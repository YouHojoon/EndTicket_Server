package ac.kr.smu.endTicket.infra.oAuth2

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.endTicket.auth.domain.service.OAuthService
import ac.kr.smu.endTicket.infra.oAuth2.exception.OAuth2RequestException
import ac.kr.smu.endTicket.infra.oAuth2.IDToken.IDTokenService
import kotlinx.coroutines.*
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody


/**
 *
 * [OAuthService](ac.kr.smu.endticket.auth.domain.service.OAuthService)의 구현체
 * @property clientRegistrationRepository OAuth 클라이언트가 저장된 객체
 */
@Service
class OAuth2ServiceImpl(
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val idTokenService: IDTokenService
): OAuthService {
    /**
     * 외부 SNS 서비스 인증을 하여 access 토큰 응답을 반환하는 메소드
     * @param socialType SNS 종류
     * @param code SNS 인증에서 반환받은 authorization code
     * @return access 토큰 응답을 반환, 에러 발생 시 null 반환
     */
    override fun oAuth(socialType: SocialType, code: String): OAuth2TokenResponse{
        println(code)
        val provider = clientRegistrationRepository.findByRegistrationId(socialType.name.lowercase())
        return runBlocking {
            getToken(provider,code)
        }
    }

    /**
     * SNS 서비스의 ID 토큰에서 SNS 회원 번호를 파싱하는 메소드
     * @param socialType SNS 종류
     * @param idToken ID 토큰
     * @return 파싱된 회원 번호
     */
    override fun parseSocialUserNumber(socialType: SocialType, idToken: String): String {
        return idTokenService.parseSocialUserNumber(socialType,idToken)
    }

    /**
     * 외부 SNS 서비스와 통신하여 응답 결과를 반환하는 메소드
     * @param provider 통신할 SNS 서비스
     * @param code SNS 인증에서 반환받은 authorization code
     * @return 통신 결과를 반환
     * @throws OAuth2RequestException OAuth 요청이 에러일 때 발생
     */
    @Throws(OAuth2RequestException::class)
    private suspend fun getToken(provider: ClientRegistration, code: String): OAuth2TokenResponse{
       try {
           return WebClient.create()
               .post()
               .uri(provider.providerDetails.tokenUri)
               .headers {
                   it.contentType = MediaType.APPLICATION_FORM_URLENCODED
               }
               .bodyValue(tokenRequest(provider, code))
               .retrieve()
               .awaitBody()
       }catch (e: WebClientResponseException){
           throw OAuth2RequestException(e.getResponseBodyAs(Map::class.java).toString(), e)
       }
    }

    /**
     * 통신에 필요한 토큰 요청 body를 반환하는 메소드
     * @param provider 통신할 SNS 서비스
     * @param code SNS 인증에서 반환받은 authorization code
     * @return 요청에 필요한 body
     */
    private fun tokenRequest(provider: ClientRegistration, code: String): LinkedMultiValueMap<String, String> {
        var body = LinkedMultiValueMap<String, String>()
        body.add("code", code)
        body.add("grant_type","authorization_code")
        body.add("redirect_uri", provider.redirectUri)
        body.add("client_secret", provider.clientSecret)
        body.add("client_id",provider.clientId)

        return body
    }
}