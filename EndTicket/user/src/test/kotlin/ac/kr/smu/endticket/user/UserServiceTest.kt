package ac.kr.smu.endticket.user

import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.protobuf.FindUserIdRequest
import ac.kr.smu.endticket.protobuf.SocialType
import ac.kr.smu.endticket.protobuf.UserServiceGrpc
import ac.kr.smu.endticket.user.domain.exception.NotFoundUserException
import ac.kr.smu.endticket.user.domain.model.User
import ac.kr.smu.endticket.user.domain.repository.UserRepository
import ac.kr.smu.endticket.user.service.UserService
import ac.kr.smu.endticket.user.ui.request.RegisterNicknameRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@SpringBootTest(
    classes = [
        UserService::class,
        GrpcConfig::class]
)
@DirtiesContext
class UserServiceTest @Autowired constructor(
    @MockBean
    private val repo: UserRepository,
    private val userService: UserService
) {
    @GrpcClient("user")
    private lateinit var userServiceClient: UserServiceGrpc.UserServiceBlockingStub
    private val SOCIAL_TYPE = User.SocialType.KAKAO

    private companion object{
        private const val SOCIAL_USER_NUMBER = "1"
        private const val NICKNAME = "닉네임"
    }

    @Test
    @DisplayName("grpc 통신을 이용한 userId 반환 테스트")
    @DirtiesContext
    fun given_socialType_and_socialUserNumber_when_findUserId_then_success(){
        val user = createUser()

        Mockito
            .`when`(repo.findBySocialTypeAndSocialUserNumber(user.socialType, SOCIAL_USER_NUMBER))
            .thenReturn(user)

        val userId = userServiceClient.findUserId(
            FindUserIdRequest.
            newBuilder()
                .setSocialUserNumber(SOCIAL_USER_NUMBER)
                .setSocialType(SocialType.valueOf(user.socialType.name))
                .build()
        ).userId

        assertEquals(user.id, userId)
    }

    @Test
    @DisplayName("가입되지 않은 사용자 userId 반환 테스트")
    @DirtiesContext
    fun given_nonRegisteredUser_when_findUserId_then_saveUser_and_success(){
        val user = createUser()
        Mockito
            .`when`(repo.findBySocialTypeAndSocialUserNumber(SOCIAL_TYPE, SOCIAL_USER_NUMBER))
            .thenReturn(null)
        Mockito.`when`(repo.save(mockAny()))
            .thenReturn(user)

        val userId = userServiceClient.findUserId(
            FindUserIdRequest.
            newBuilder()
                .setSocialUserNumber(SOCIAL_USER_NUMBER)
                .setSocialType(SocialType.valueOf(SOCIAL_TYPE.name))
                .build()
        ).userId

        Mockito.verify(repo).save(mockAny())
        assertEquals(user.id, userId)
    }

    @Test
    @DisplayName("닉네임 등록 테스트")
    fun given_nickname_and_userID_when_updateNickname_then_updateNicknameOfUser(){
        val request = RegisterNicknameRequest(NICKNAME)
        val user = createUser()

        Mockito.
                `when`(repo.findById(user.id))
                .thenReturn(Optional.of(user))

        userService.registerNickname(request,user.id)
        assertEquals(repo.findById(user.id).get().nickname, request.nickname)

    }

    @Test
    @DisplayName("닉네임이 등록되어 있을 시 닉네임 변경 테스트")
    fun given_userWithNicknameAlreadyRegistered_when_registerNickname_then_throwIllegalStateException(){
        val request = RegisterNicknameRequest(NICKNAME)
        val user = User(SOCIAL_TYPE, SOCIAL_USER_NUMBER, request.nickname)

        Mockito
            .`when`(repo.findById(user.id))
            .thenReturn(Optional.of(user))

        assertThrows<IllegalStateException> {
            userService.registerNickname(request, user.id)
        }
    }
    @Test
    @DisplayName("존재하지 않는 사용자 닉네임 등록 테스트")
    fun given_notExistUser_when_registerNickname_then_throwNotFoundUserException() {
        Mockito.`when`(repo.findById(Mockito.anyLong()))
            .thenReturn(Optional.empty())

        assertThrows<NotFoundUserException> { userService.registerNickname(RegisterNicknameRequest(NICKNAME), 1L)}
    }
    private fun createUser() = User(SOCIAL_TYPE, SOCIAL_USER_NUMBER)

}
