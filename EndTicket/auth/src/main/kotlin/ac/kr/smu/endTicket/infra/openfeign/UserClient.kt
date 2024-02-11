package ac.kr.smu.endTicket.infra.openfeign

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "user", url = "\${openfeign.client.user.url}")
interface UserClient {
    fun getUserId(@PathVariable socialUserNumber: Long): Long?
}