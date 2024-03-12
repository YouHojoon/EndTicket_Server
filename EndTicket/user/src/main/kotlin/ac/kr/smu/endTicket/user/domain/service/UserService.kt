package ac.kr.smu.endTicket.user.domain.service

import ac.kr.smu.endTicket.user.domain.exception.UserAlreadyExistException
import ac.kr.smu.endTicket.user.domain.model.User
import ac.kr.smu.endTicket.user.domain.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.SQLIntegrityConstraintViolationException
import kotlin.jvm.optionals.getOrNull

/**
 * 사용자 관련 서비스 제공 클래스
 * @property userRepo 의존성 주입으로 얻는 user 저장소
 */
@Service
class UserService(
    private val userRepo: UserRepository
) {

    /**
     * 사용자 생성 메소드
     * @param user 생성할 사용자
     * @return 생성된 사용자 번호
     * @throws UserAlreadyExistException 이미 가입된 SNS 사용자일 시
     */
    @Throws(UserAlreadyExistException::class)
    fun createUser(user: User): Long{
        try{
            return userRepo.save(user).id
        }catch (e: SQLIntegrityConstraintViolationException) {
            throw UserAlreadyExistException()
        }
    }

    /**
     * SNS 사용자 번호를 통해 해당 SNS의 사용자를 찾아 user id를 반환하는 메소드
     * @param socialType 해당 SNS로 회원가입한 사용자
     * @param socialUserNumber SNS 사용자 번호
     * @return 사용자 번호 반환, 존재하지 않는다면 null 반환
     */
    fun findIdBySocialTypeAndSocialUserNumber(socialType: User.SocialType, socialUserNumber: String): Long?{
        return userRepo.findBySocialTypeAndSocialUserNumber(socialType, socialUserNumber)?.id
    }
}