package ac.kr.smu.endticket.user

import ac.kr.smu.endticket.user.domain.model.User
import ac.kr.smu.endticket.user.ui.request.RegisterNicknameRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UserTest {
    @Test
    @DisplayName("사용자의 닉네임이 등록되어 있을 시 수정 테스트")
    fun given_userWithNicknameAlreadyRegistered_when_registerNickname_then_throwIllegalStateException(){
        val user = User(User.SocialType.KAKAO, "1", "닉네임")
        assertThrows<IllegalStateException> {
            user.registerNickname(
                RegisterNicknameRequest(
                    "닉네임1"
                )
            )
        }
    }
}