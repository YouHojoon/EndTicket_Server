package ac.kr.smu.endticket.futureme.config

import ac.kr.smu.endticket.common.web.swagger.AccessTokenSecurityScheme
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.servers.Server
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@AccessTokenSecurityScheme
@OpenAPIDefinition(
    info =
        Info(
            title = "EndTicket",
            description = "미래의 나 서버 API 명세서",
            contact = Contact(name = "유호준", email = "dbghwns11@gmail.com"),
        ),
)
class SwaggerConfig(
    private val discoveryClient: DiscoveryClient,
) {
    @Bean
    fun openAPI(): OpenAPI =
        OpenAPI().servers(
            discoveryClient.getInstances("gateway").map {
                Server().url(it.uri.toString()).description("gateway")
            },
        )
}
