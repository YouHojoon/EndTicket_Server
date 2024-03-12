package ac.kr.smu.endTicket.infra.openfeign

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import org.apache.catalina.User
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "user", url = "\${openfeign.client.user.url}/users")
interface UserClient {
    @GetMapping("/{SNS}/{socialUserNumber}")
    fun getUserId(@PathVariable("SNS") SNS: SocialType, @PathVariable("socialUserNumber") socialUserNumber: String): GetUserIDResponse

    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): Long
}