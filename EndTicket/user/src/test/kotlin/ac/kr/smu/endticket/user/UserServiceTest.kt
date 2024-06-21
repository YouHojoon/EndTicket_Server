package ac.kr.smu.endticket.user

import ac.kr.smu.endticket.common.test.mockAny
import ac.kr.smu.endticket.protobuf.FindUserIdRequest
import ac.kr.smu.endticket.protobuf.SocialType
import ac.kr.smu.endticket.protobuf.UserServiceGrpc
import ac.kr.smu.endticket.user.domain.model.User
import ac.kr.smu.endticket.user.domain.repository.UserRepository
import ac.kr.smu.endticket.user.service.UserService
import ac.kr.smu.endticket.user.ui.request.NicknameRegisterRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.annotation.DirtiesContext
import java.util.*
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    @ParameterizedTest
    @DisplayName("grpc 통신을 이용한 userId 반환 테스트")
    @DirtiesContext
    @MethodSource("${UserTestParameters.PATH}#provideUser")
    fun given_socialType_and_socialUserNumber_when_findUserId_then_returnUserId(user: User){
        Mockito
            .`when`(repo.findBySocialTypeAndSocialUserNumber(user.socialType, UserTestParameters.SOCIAL_USER_NUMBER))
            .thenReturn(user)

        val userId = userServiceClient.findUserId(
            FindUserIdRequest.
            newBuilder()
                .setSocialUserNumber(UserTestParameters.SOCIAL_USER_NUMBER)
                .setSocialType(SocialType.valueOf(user.socialType.name))
                .build()
        ).userId

        assertEquals(user.id, userId)
    }

    @ParameterizedTest
    @DisplayName("가입되지 않은 사용자 userId 반환 테스트")
    @DirtiesContext
    @MethodSource("${UserTestParameters.PATH}#provideUser")
    fun given_nonRegisteredUser_when_findUserId_then_saveUserAndReturnUserId(user: User){
        Mockito
            .`when`(repo.findBySocialTypeAndSocialUserNumber(UserTestParameters.SOCIAL_TYPE, UserTestParameters.SOCIAL_USER_NUMBER))
            .thenReturn(null)
        Mockito.`when`(repo.save(mockAny()))
            .thenReturn(user)

        val userId = userServiceClient.findUserId(
            FindUserIdRequest.
            newBuilder()
                .setSocialUserNumber(UserTestParameters.SOCIAL_USER_NUMBER)
                .setSocialType(SocialType.valueOf(user.socialType.name))
                .build()
        ).userId

        Mockito.verify(repo).save(mockAny())
        assertEquals(user.id, userId)
    }

    @ParameterizedTest
    @DisplayName("닉네임 등록 테스트")
    @MethodSource("${UserTestParameters.PATH}#provideUserWithoutNickname")
    fun given_nickname_and_userID_when_updateNickname_then_updateNicknameOfUser(user: User){
        val request = NicknameRegisterRequest(UserTestParameters.NICKNAME)

        Mockito.
                `when`(repo.findById(user.id))
                .thenReturn(Optional.of(user))

        userService.registerNickname(request,user.id)
        assertEquals(repo.findById(user.id).get().nickname, request.nickname)

    }

    @ParameterizedTest
    @DisplayName("비정상적인 사용자 닉네임 변경 테스트")
    @MethodSource("${UserTestParameters.PATH}#provideInvalidUserAndException")
    fun given_userWithNicknameAlreadyRegistered_when_registerNickname_then_throwIllegalStateException(user: User?, exception: KClass<out Throwable>){
        val request = NicknameRegisterRequest(UserTestParameters.NICKNAME)
        val id = user?.id ?: 1L
        Mockito
            .`when`(repo.findById(id))
            .thenReturn(Optional.ofNullable(user))

        assertFailsWith(exception) { userService.registerNickname(request, id) }
    }
}
