package ac.kr.smu.endTicket

import ac.kr.smu.endTicket.user.domain.model.User
import ac.kr.smu.endTicket.user.domain.repository.UserRepository
import ac.kr.smu.endTicket.user.domain.service.UserService
import ac.kr.smu.endTicket.user.ui.request.RegisterNicknameRequest
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
import java.util.*
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class UserServiceTest(
    @Mock
    private val userRepo: UserRepository
) {
    @Rule
    private val grpcCleanup = GrpcCleanupRule()
    @InjectMocks
    private lateinit var userService: UserService
    private lateinit var blockingStub: UserServiceGrpc.UserServiceBlockingStub

    private companion object{
        private const val SOCIAL_USER_NUMBER = "1"
        private val SOCIAL_TYPE = User.SocialType.KAKAO
        private const val USER_ID = 1L
    }

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
    @DisplayName("닉네임 등록 테스트")
    fun given_nickname_and_userID_when_updateNickname_then_updateNicknameOfUser(){
        val request = RegisterNicknameRequest("닉네임")
        Mockito.
                `when`(userRepo.findById(USER_ID))
                .thenReturn(Optional.of(createUser()))

        userService.registerNickname(request,USER_ID)
        assertEquals(userRepo.findById(USER_ID).get().nickname, request.nickname)

    }

    @Test
    @DisplayName("닉네임이 등록되어 있을 시 닉네임 변경 테스트")
    fun given_nickname_and_userID_of_user_whoseNicknameIsNotNull_when_updateNickname_then_throw_IllegalStateException(){
        val request = RegisterNicknameRequest("닉네임")

        Mockito
            .`when`(userRepo.findById(USER_ID))
            .thenReturn(Optional.of(User(SOCIAL_TYPE, SOCIAL_USER_NUMBER, USER_ID, request.nickname)))

        assertThrows<IllegalStateException> {
            userService.registerNickname(request, USER_ID)
        }
    }

    private fun createUser(): User{
        return User(SOCIAL_TYPE,SOCIAL_USER_NUMBER,USER_ID)
    }
}
