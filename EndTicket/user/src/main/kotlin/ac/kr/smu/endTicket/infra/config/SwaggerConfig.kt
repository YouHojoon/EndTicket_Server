package ac.kr.smu.endTicket.infra.config

import ac.kr.smu.endTicket.user.domain.model.User
import ac.kr.smu.endTicket.user.ui.dto.BindingExceptionDTO
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(info = Info(title = "EndTicket", description = "유저 서버 API 명세서", contact = Contact(name = "유호준", email = "dbghwns11@gmail.com")))
class SwaggerConfig {

    @Bean
    fun groupedOpenApi(): GroupedOpenApi{
        return GroupedOpenApi
            .builder()
            .group("유저 서버 API")
            .pathsToMatch("/users/**")
            .build()
    }
}