package ac.kr.smu.endTicket.ticket.infra.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(info = Info(title = "EndTicket", description = "티켓 서버 API 명세서", contact = Contact(name = "유호준", email = "dbghwns11@gmail.com")))
@SecurityScheme(
    type = SecuritySchemeType.HTTP,
    name = "Access token",
    bearerFormat = "JWT",
    scheme = "bearer"
)
class SwaggerConfig {
}