package ac.kr.smu.endTicket.infra.oAuth

import ac.kr.smu.endTicket.auth.domain.model.SocialType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/oauth")
class OAuthController {

    @GetMapping("/{SNS}")
    fun callback(@PathVariable SNS: SocialType, @RequestParam code: String): ResponseEntity<Void>{
        println(code)
        return ResponseEntity.ok().build()
    }
}