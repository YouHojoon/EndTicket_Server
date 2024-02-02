package ac.kr.smu.endTicket.user.domain.service

import ac.kr.smu.endTicket.user.domain.exception.UserEmailDuplicationException
import ac.kr.smu.endTicket.user.domain.model.User
import ac.kr.smu.endTicket.user.domain.repository.UserRepository
import org.springframework.stereotype.Service
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
     * @throws UserEmailDuplicationException 이메일이 중복됐을 시
     */
    @Throws(UserEmailDuplicationException::class)
    fun createUser(user: User){
        if (checkUserExistenceByEmail(user.email)) throw UserEmailDuplicationException()

        userRepo.save(user)
    }

    /**
     * 사용자의 이메일을 통해서 사용자가 존재하는 지 확인하는 메소드
     * 중복 이메일을 방지하기 위한 메소드
     * @return 사용자 존재 여부
     */
    fun checkUserExistenceByEmail(email: String): Boolean{
        return userRepo.findByEmail(email) != null
    }
}