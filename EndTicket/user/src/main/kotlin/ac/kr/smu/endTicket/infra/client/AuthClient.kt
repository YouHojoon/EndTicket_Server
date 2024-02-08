package ac.kr.smu.endTicket.infra.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "auth", url = "\${feign.client.auth.url}")
interface AuthClient {
    fun getSocialUserNumber(@RequestParam code: String): String
}