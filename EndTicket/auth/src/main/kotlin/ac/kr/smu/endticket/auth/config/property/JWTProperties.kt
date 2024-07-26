package ac.kr.smu.endticket.auth.config.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("jwt")
class JWTProperties(
    val secret: String,
    val accessTokenExpiration: Long,
    val refreshTokenExpiration: Long,
    val refreshTokenReissueExpiration: Long,
)
