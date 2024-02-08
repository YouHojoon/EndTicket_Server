package ac.kr.smu.endTicket.infra.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
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