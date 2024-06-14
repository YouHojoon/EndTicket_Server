package ac.kr.smu.endTicket.common.web.swagger

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme

/**
 * Swagger API 문서의 SecurityScheme에 Access token을 등록하는 어노테이션
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SecurityScheme(
    name = "Access token",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
annotation class AccessTokenSecurityScheme