package ac.kr.smu.endTicket.infra.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(info = Info(title = "EndTicket", description = "인증 서버 API 명세서", contact = Contact(name = "유호준", email = "dbghwns11@gmail.com")))
class SwaggerConfig {
}