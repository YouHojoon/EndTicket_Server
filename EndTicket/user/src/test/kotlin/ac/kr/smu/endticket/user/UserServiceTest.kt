package ac.kr.smu.endticket.user

import ac.kr.smu.endticket.protobuf.FindUserIdRequest
import ac.kr.smu.endticket.protobuf.SocialType
import ac.kr.smu.endticket.protobuf.UserServiceGrpc
import ac.kr.smu.endticket.user.domain.exception.NotFoundUserException
import ac.kr.smu.endticket.user.domain.model.User
import ac.kr.smu.endticket.user.domain.repository.UserRepository
import ac.kr.smu.endticket.user.service.UserService
import ac.kr.smu.endticket.user.ui.request.RegisterNicknameRequest
import io.grpc.ManagedChannel
import io.grpc.Server
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
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class UserServiceTest(
    @Mock
    private val userRepo: UserRepository
) {
    @InjectMocks
    private lateinit var userService: UserService
    private lateinit var blockingStub: UserServiceGrpc.UserServiceBlockingStub
    private lateinit var grpcServer: Server
    private lateinit var channel: ManagedChannel
    private val SOCIAL_TYPE = User.SocialType.KAKAO

    private companion object{
        private const val SOCIAL_USER_NUMBER = "1"
        private const val NICKNAME = "닉네임"
    }

    @BeforeEach
    fun init(){
        MockitoAnnotations.openMocks(this)
    }


    @Test
    @DisplayName("grpc 통신을 이용한 userID 반환 테스트")
    fun given_socialType_and_socialUserNumber_when_findUserID_then_returnUserID(){
        val user = createUser()
        setGrpc()

        Mockito
            .`when`(userRepo.findBySocialTypeAndSocialUserNumber(user.socialType, SOCIAL_USER_NUMBER))
            .thenReturn(user)

        val userIDResponse = blockingStub.findUserId(
            FindUserIdRequest
            .newBuilder()
            .setSocialType(SocialType.valueOf(user.socialType.name))
            .setSocialUserNumber(SOCIAL_USER_NUMBER).build()
        )

        assertEquals(userIDResponse.userId, user.id)
        shutdownGrpc()
    }

    @Test
    @DisplayName("닉네임 등록 테스트")
    fun given_nickname_and_userID_when_updateNickname_then_updateNicknameOfUser(){
        val request = RegisterNicknameRequest(NICKNAME)
        val user = createUser()

        Mockito.
                `when`(userRepo.findById(user.id))
                .thenReturn(Optional.of(user))

        userService.registerNickname(request,user.id)
        assertEquals(userRepo.findById(user.id).get().nickname, request.nickname)

    }

    @Test
    @DisplayName("닉네임이 등록되어 있을 시 닉네임 변경 테스트")
    fun given_userWithNicknameAlreadyRegistered_when_registerNickname_then_throwIllegalStateException(){
        val request = RegisterNicknameRequest(NICKNAME)
        val user = User(SOCIAL_TYPE, SOCIAL_USER_NUMBER, request.nickname)

        Mockito
            .`when`(userRepo.findById(user.id))
            .thenReturn(Optional.of(user))

        assertThrows<IllegalStateException> {
            userService.registerNickname(request, user.id)
        }
    }
    @Test
    @DisplayName("존재하지 않는 사용자 닉네임 등록 테스트")
    fun given_notExistUser_when_registerNickname_then_throwNotFoundUserException() {
        Mockito.`when`(userRepo.findById(Mockito.anyLong()))
            .thenReturn(Optional.empty())

        assertThrows<NotFoundUserException> { userService.registerNickname(RegisterNicknameRequest(NICKNAME), 1L)}
    }
    private fun createUser() = User(SOCIAL_TYPE, SOCIAL_USER_NUMBER)
    private fun setGrpc(){
        val server = InProcessServerBuilder.generateName()
        grpcServer = InProcessServerBuilder
            .forName(server).directExecutor()
            .addService(userService).build().start()

        channel = InProcessChannelBuilder.forName(server).directExecutor().build()
        blockingStub = UserServiceGrpc.newBlockingStub(channel)
    }
    private fun shutdownGrpc(){
        grpcServer.shutdownNow()
        channel.shutdownNow()
    }
}
