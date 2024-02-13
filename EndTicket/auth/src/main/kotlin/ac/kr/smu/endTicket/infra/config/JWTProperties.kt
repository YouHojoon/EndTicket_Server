package ac.kr.smu.endTicket.infra.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component


@ConfigurationProperties("jwt")
class JWTProperties(
    val secret: String,
    val accessTokenExpiration: Long,
    val refreshTokenExpiration: Long
)