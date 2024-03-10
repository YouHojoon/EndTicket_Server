package ac.kr.smu.endTicket.infra.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(info = Info(title = "EndTicket", description = "인증 서버 API 명세서", contact = Contact(name = "유호준", email = "dbghwns11@gmail.com")))
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI{
        return OpenAPI().components(
            Components()
                .addSecuritySchemes(
                    "Bearer Token",
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("Bearer")
                    .bearerFormat("JWT")
                    .`in`(SecurityScheme.In.HEADER)
                    .name("Authorization")
                )
        )
    }
}