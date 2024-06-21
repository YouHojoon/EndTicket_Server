package ac.kr.smu.endticket.user

import ac.kr.smu.endticket.user.domain.model.User
import ac.kr.smu.endticket.user.ui.request.NicknameRegisterRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class UserTest {
    @ParameterizedTest
    @DisplayName("사용자의 닉네임이 등록되어 있을 시 수정 테스트")
    @MethodSource("${UserTestParameters.PATH}#provideUser")
    fun given_userWithNicknameAlreadyRegistered_when_registerNickname_then_throwIllegalStateException(user: User){
        assertThrows<IllegalStateException> {
            user.registerNickname(NicknameRegisterRequest("닉네임1"))
        }
    }
}