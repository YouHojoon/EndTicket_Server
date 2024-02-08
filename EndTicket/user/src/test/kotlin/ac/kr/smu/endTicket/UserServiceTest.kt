package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.user.domain.exception.UserEmailDuplicationException
import ac.kr.smu.endTicket.user.domain.model.User
import ac.kr.smu.endTicket.user.domain.repository.UserRepository
import ac.kr.smu.endTicket.user.domain.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import java.sql.SQLIntegrityConstraintViolationException
import java.util.*
import kotlin.test.Test

@ExtendWith(MockitoExtension::class)
class UserServiceTest(
    @Mock
    private val userRepo: UserRepository,
) {
    @InjectMocks
    lateinit var userService: UserService

    @BeforeEach
    fun init(){
        MockitoAnnotations.openMocks(this)
    }

    @Test
    @DisplayName("이메일 중복된 사용자 생성 테스트")
//    @Throws(SQLIntegrityConstraintViolationException::class)
    @kotlin.jvm.Throws(SQLIntegrityConstraintViolationException::class)
    fun when_사용자_이메일이_중복됐을때_then_throw_UserEmailDuplicationException() {

        val user = createUser()
        Mockito
            .`when`(userRepo
                .save(user))
            .thenAnswer{
                throw SQLIntegrityConstraintViolationException()
            }

        assertThrows<UserEmailDuplicationException> {
            userService.createUser(user)
        }
    }

    private fun createUser(): User{
        return User("test","test@test.com", User.SocialType.KAKAO,"1")
    }
}
