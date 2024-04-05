package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.user.domain.exception.UserAlreadyExistException
import ac.kr.smu.endTicket.user.domain.model.User
import ac.kr.smu.endTicket.user.domain.repository.UserRepository
import ac.kr.smu.endTicket.user.domain.service.UserService
import ac.kr.smu.protobuf.FindUserIDRequest
import ac.kr.smu.protobuf.SocialType
import ac.kr.smu.protobuf.UserServiceGrpc
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
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
import kotlin.test.Test
import io.grpc.testing.GrpcCleanupRule
import org.junit.Rule
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class UserServiceTest(
    @Mock
    private val userRepo: UserRepository
) {
    @Rule
    private val grpcCleanup = GrpcCleanupRule()
    @InjectMocks
    lateinit var userService: UserService
    lateinit var blockingStub: UserServiceGrpc.UserServiceBlockingStub
    private val SOCIAL_USER_NUMBER = "1"
    @BeforeEach
    fun init(){
        MockitoAnnotations.openMocks(this)

        val server = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder
                .forName(server).directExecutor()
                .addService(userService).build().start()
        )

        blockingStub = UserServiceGrpc.newBlockingStub(
            grpcCleanup.register(
                InProcessChannelBuilder.forName(server).directExecutor().build()
            )
        )
    }

    @Test
    @DisplayName("user id grpc 테스트")
    fun given_socialType_and_socialUserNumber_when_findUserID_then_returnUserID(){
        val user = createUser()

        Mockito
            .`when`(userRepo.findBySocialTypeAndSocialUserNumber(user.socialType, SOCIAL_USER_NUMBER))
            .thenReturn(user)

        val userIDResponse = blockingStub.findUserID(
            FindUserIDRequest
            .newBuilder()
            .setSocialType(SocialType.valueOf(user.socialType.name))
            .setSocialUserNumber(SOCIAL_USER_NUMBER).build()
        )

        assertEquals(userIDResponse.userId, user.id)
    }
    @Test
    @DisplayName("Social User Number로 user id 반환 테스트")
    fun givenSocialUserNumber_then_returnUserId() {
        val user = createUser()
        Mockito
            .`when`(userRepo.findBySocialTypeAndSocialUserNumber(User.SocialType.KAKAO, SOCIAL_USER_NUMBER))
            .thenReturn(user)

        assert(userService.findIdBySocialTypeAndSocialUserNumber(User.SocialType.KAKAO,SOCIAL_USER_NUMBER) == user.id)

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
        return User(User.SocialType.KAKAO,SOCIAL_USER_NUMBER,1)
    }
}
