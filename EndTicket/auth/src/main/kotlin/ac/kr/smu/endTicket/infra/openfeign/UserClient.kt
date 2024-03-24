package ac.kr.smu.endTicket.infra.openfeign

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "user", url = "\${openfeign.client.user.url}/users")
interface UserClient {
    @GetMapping("/{SNS}/{socialUserNumber}")
    fun getUserId(@PathVariable("SNS") socialType: SocialType, @PathVariable("socialUserNumber") socialUserNumber: String): UserIDResponse

    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): UserIDResponse
}