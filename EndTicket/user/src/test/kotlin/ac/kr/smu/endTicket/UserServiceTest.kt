package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.user.domain.exception.UserAlreadyExistException
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
            .`when`(userRepo.findBySocialTypeAndSocialUserNumber(User.SocialType.KAKAO, "1"))
            .thenReturn(user)

        assert(userService.findIdBySocialTypeAndSocialUserNumber(User.SocialType.KAKAO,"1") == user.id)

    }

    @Test
    @DisplayName("이미 가입된 SNS 이용자에 대한 테스트")
    fun givenDuplicateSocialUserNumberWithSameSocialType_then_throwUserAlreadyExistException() {
        val user = createUser()
        Mockito
            .`when`(userRepo.save(user))
            .thenAnswer {
                throw SQLIntegrityConstraintViolationException()
            }

        assertThrows<UserAlreadyExistException> { userService.createUser(user) }
    }

    private fun createUser(): User{
        return User(User.SocialType.KAKAO,"1")
    }
}
