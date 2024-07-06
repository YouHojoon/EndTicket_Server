package ac.kr.smu.endticket.user.domain.repository

import ac.kr.smu.endticket.user.domain.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findBySocialTypeAndSocialUserNumber(
        socialType: User.SocialType,
        socialUserNumber: String,
    ): User?
}
