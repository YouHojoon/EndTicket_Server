package ac.kr.smu.endticket.auth

import ac.kr.smu.endticket.auth.infra.oauth2.OAuth2AccessTokenRequestConverter
import ac.kr.smu.endticket.auth.infra.oauth2.OAuth2LoginAuthenticationConverter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames

class OAuth2AccessTokenRequestConverterTest : DescribeSpec() {
    private val clientRegistrationRepository = mockk<ClientRegistrationRepository>()
    private val authenticationManager = mockk<AuthenticationManager>()
    private val loginAuthenticationConverter = mockk<OAuth2LoginAuthenticationConverter>()

    private companion object {
        const val TOKEN_ENDPOINT = "/auth/oauth2/token/**"
        const val REGISTRATION_ID = "kakao"
    }

    private val stub =
        OAuth2AccessTokenRequestConverter(
            clientRegistrationRepository,
            authenticationManager,
            TOKEN_ENDPOINT,
        ) {
            loginAuthenticationConverter
        }

    init {
        every { clientRegistrationRepository.findByRegistrationId(REGISTRATION_ID) } returns mockClientRegistration()
        every { loginAuthenticationConverter.convert(any()) } returns mockk()

        describe("Access Token 발급 요청 시") {
            context("grant_type이 authorization code 이고") {
                context("code가 있을 때") {
                    val code = "code"

                    context("등록되어 있는 registration id 라면") {
                        val request = mockHttpServletRequest(code = code)

                        context("authenticationManager가 오류를 발생하지 않으면") {
                            it("인증에 성공한다.") {
                                stub.convert(request) shouldNotBe null
                            }
                        }

                        context("authenticationManager가 오류를 발생하면") {
                            every { authenticationManager.authenticate(any()) } throws RuntimeException()

                            it("해당 오류를 던진다.") {
                                shouldThrow<RuntimeException> { stub.convert(request) }
                            }
                        }
                    }

                    context("등록되어 있는 registraion id가 아니라면") {
                        val registrationId = "X"
                        every { clientRegistrationRepository.findByRegistrationId(registrationId) } returns null

                        it("OAuth2AuthenticationException이 발생한다.") {
                            shouldThrow<OAuth2AuthenticationException> {
                                stub.convert(
                                    mockHttpServletRequest(
                                        "/auth/oauth2/token/code/$registrationId",
                                        code = code,
                                    ),
                                )
                            }
                        }
                    }

                    context("registration id가 없다면") {
                        it("OAuth2AuthenticationException이 발생한다.") {
                            shouldThrow<OAuth2AuthenticationException> {
                                stub.convert(
                                    mockHttpServletRequest(
                                        "/auth/oauth2/token/code/ ",
                                        code = code,
                                    ),
                                )
                            }
                        }
                    }
                }

                context("code가 없다면") {
                    it("OAuth2AuthenticationException이 발생한다.") {
                        shouldThrow<OAuth2AuthenticationException> { stub.convert(mockHttpServletRequest()) }
                    }
                }
            }

            context("grant_type이 authorization code가 아닐 때") {
                it("인증에 실패한다.") {
                    stub.convert(mockHttpServletRequest(grantType = AuthorizationGrantType.REFRESH_TOKEN)) shouldBe null
                }
            }
        }

        describe("Access Token 발급 요청이 아닐 때") {
            it("인증에 실패한다.") {
                stub.convert(mockHttpServletRequest("/")) shouldBe null
            }
        }
    }

    private fun mockHttpServletRequest(
        requestUri: String = "/auth/oauth2/token/code/$REGISTRATION_ID",
        grantType: AuthorizationGrantType = AuthorizationGrantType.AUTHORIZATION_CODE,
        code: String? = null,
    ) = mockk<HttpServletRequest>().apply {
        every { getParameter(OAuth2ParameterNames.GRANT_TYPE) } returns grantType.value
        every { requestURI } returns requestUri
        every { servletPath } returns requestUri
        every { pathInfo } returns ""
        every { method } returns HttpMethod.POST.name()
        every { getParameterValues(OAuth2ParameterNames.CODE) } returns if (code == null) emptyArray() else arrayOf(code)
        every { authenticationManager.authenticate(any()) } returns mockk<OAuth2LoginAuthenticationToken>()
    }

    private fun mockClientRegistration() =
        mockk<ClientRegistration>().apply {
            every { clientId } returns REGISTRATION_ID
            every { providerDetails } returns mockProviderDetails()
            every { redirectUri } returns "/"
        }

    private fun mockProviderDetails(): ProviderDetails =
        mockk<ProviderDetails>().apply {
            every { authorizationUri } returns "/"
        }
}
