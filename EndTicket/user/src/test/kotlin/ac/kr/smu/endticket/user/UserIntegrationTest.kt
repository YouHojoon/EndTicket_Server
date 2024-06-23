package ac.kr.smu.endticket.user

import ac.kr.smu.endticket.common.web.aop.BindExceptionAdvice
import ac.kr.smu.endticket.common.web.test.expectBindException
import ac.kr.smu.endticket.common.web.test.expectExceptionResponse
import ac.kr.smu.endticket.user.domain.exception.UserNotFoundException
import ac.kr.smu.endticket.user.domain.model.User
import ac.kr.smu.endticket.user.domain.repository.UserRepository
import ac.kr.smu.endticket.user.service.UserService
import ac.kr.smu.endticket.user.ui.controller.UserController
import ac.kr.smu.endticket.user.ui.request.NicknameRegisterRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import kotlin.properties.Delegates

@SpringBootTest(
    classes = [
        UserController::class,
        UserService::class,
        UserRepository::class,
        DataSourceAutoConfiguration::class,
        HibernateJpaAutoConfiguration::class,
        TransactionAutoConfiguration::class
    ]
)
@EnableJpaRepositories("ac.kr.smu.endTicket.user.domain.repository")
@EntityScan("ac.kr.smu.endTicket.user.domain.model")
class UserIntegrationTest @Autowired constructor(
    controller: UserController,
    private val repo: UserRepository
) {
    private val mvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(BindExceptionAdvice())
            .build()

    private var id by Delegates.notNull<Long>()

    @BeforeEach
    fun init(){
        id = repo.save(User(User.SocialType.KAKAO, "1")).id
    }
    @AfterEach
    fun reset(){
        repo.deleteAll()
    }

    @Test
    @DisplayName("닉네임 등록")
    fun given_nickname_when_registerNickname_then_success(){
        val request = NicknameRegisterRequest(UserTestParameters.NICKNAME)

        mvc
            .registerNickname(request,id)
            .andExpect(MockMvcResultMatchers.status().isNoContent)
    }

    @ParameterizedTest
    @DisplayName("부적절한 닉네임 등록 테스트")
    @MethodSource("${UserTestParameters.PATH}#provideInvalidNicknameRegisterRequest")
    fun given_invalidNickname_when_updateNickname_then_responseBindExceptionResponse(request: NicknameRegisterRequest){
        mvc.registerNickname(request, id).expectBindException()
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 닉네임 등록 테스트")
    fun given_notExistUser_when_registerNickname_then_responseExceptionResponseWithStatus404(){
        val request = NicknameRegisterRequest(UserTestParameters.NICKNAME)

        mvc
            .registerNickname(request, 999L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }

    @Test
    @DisplayName("닉네임이 등록되어 있는 사용자의 닉네임 등록 테스트")
    fun given_userWithNicknameAlreadyRegistered_when_registerNickname_then_responseExceptionResponseWithStatus409(){
        val request = NicknameRegisterRequest(UserTestParameters.NICKNAME)

        mvc
            .registerNickname(request, id)

        mvc
            .registerNickname(request,id)
            .andExpect(MockMvcResultMatchers.status().isConflict)
            .expectExceptionResponse()
    }


    @Test
    @DisplayName("사용자 닉네임 조회 테스트")
    fun given_id_when_findNickname_then_responseNickname(){
        val request = NicknameRegisterRequest(UserTestParameters.NICKNAME)

        mvc.registerNickname(request, id)

        mvc.findNickname(id)
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("nickname").value(request.nickname))
    }

    @Test
    @DisplayName("존재하지 않는 사용자 닉네임 조회 테스트")
    fun given_idOfNonExistentUser_when_findNickname_then_responseExceptionResponseWithStatus404(){
        mvc.findNickname(999L)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .expectExceptionResponse()
    }
}