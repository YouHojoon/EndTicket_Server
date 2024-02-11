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
    private val userRepo: UserRepository
) {
    @InjectMocks
    lateinit var userService: UserService

    @BeforeEach
    fun init(){
        MockitoAnnotations.openMocks(this)
    }

    @Test
    @DisplayName("Social User Number로 user id 반환 테스트")
    fun givenSocialUserNumber_then_returnUserId() {
        val user = createUser()
        Mockito
            .`when`(userRepo.findBySocialTypeAndSocialUserNumber(User.SocialType.KAKAO, 1))
            .thenReturn(user)

        assert(userService.findBySocialTypeAndSocialUserNumber(User.SocialType.KAKAO,1) == user.id)

    }

    private fun createUser(): User{
        return User("test", User.SocialType.KAKAO,1)
    }
}
