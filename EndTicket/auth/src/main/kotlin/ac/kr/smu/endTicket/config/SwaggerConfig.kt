package ac.kr.smu.endTicket.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@OpenAPIDefinition(info = Info(title = "EndTicket", description = "인증 서버 API 명세서", contact = Contact(name = "유호준", email = "dbghwns11@gmail.com")))
class SwaggerConfig(
    private val discoveryClient: DiscoveryClient
) {
    @Bean
    fun openAPI(): OpenAPI{
        return OpenAPI().servers(
            discoveryClient.getInstances("gateway").map {
                Server().url(it.uri.toString()).description("gateway")
            }
        )
    }
}