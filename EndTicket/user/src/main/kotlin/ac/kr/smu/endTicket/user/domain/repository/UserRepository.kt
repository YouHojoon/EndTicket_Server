package ac.kr.smu.endTicket.user.domain.repository

import ac.kr.smu.endTicket.user.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository: JpaRepository<User, Long> {
    fun findBySocialTypeAndSocialUserNumber(socialType: User.SocialType, socialUserNumber: Long): User?
}