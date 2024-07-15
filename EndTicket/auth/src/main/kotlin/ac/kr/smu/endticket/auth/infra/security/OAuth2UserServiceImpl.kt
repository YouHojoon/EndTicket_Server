package ac.kr.smu.endticket.auth.infra.security

import ac.kr.smu.endticket.auth.domain.converter.SocialTypeConverter
import ac.kr.smu.endticket.auth.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2ErrorCodes
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser

class OAuth2UserServiceImpl(
    private val userService: UserService
): OAuth2UserService<OidcUserRequest, OidcUser>  {
    private val socialTypeConverter = SocialTypeConverter()
    private val log = LoggerFactory.getLogger(OAuth2UserServiceImpl::class.java)
    private companion object{
        private val NAME_ATTRIBUTE_KEY = "user_id"
    }

    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val socialUserNumber = userRequest.idToken.subject
        val socialType = socialTypeConverter.convert(userRequest.clientRegistration.clientName) ?:
        throw OAuth2AuthenticationException(
            OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT)
        )

        try {
            val userId = userService.findUserId(socialType, socialUserNumber).get()
            val info = OidcUserInfo(mutableMapOf<String, Any>(NAME_ATTRIBUTE_KEY to userId))

            return DefaultOidcUser(
                mutableSetOf(),
                userRequest.idToken,
                info,
                NAME_ATTRIBUTE_KEY
            )
        }catch (e: Exception){
            log.error("사용자 서비스와 통신 실패",e)
            throw e
        }
    }
}