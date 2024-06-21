package ac.kr.smu.endticket.user

import ac.kr.smu.endticket.user.domain.exception.UserNotFoundException
import ac.kr.smu.endticket.user.domain.model.User
import ac.kr.smu.endticket.user.ui.request.NicknameRegisterRequest
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

object UserTestParameters{
    const val PATH  = "ac.kr.smu.endticket.user.UserTestParameters"
    const val SOCIAL_USER_NUMBER = "1"
    const val NICKNAME = "닉네임"
    val SOCIAL_TYPE = User.SocialType.KAKAO

    @JvmStatic
    fun provideUser() = Stream.of(Arguments.of(User(SOCIAL_TYPE, SOCIAL_USER_NUMBER, NICKNAME)))

    @JvmStatic
    fun provideUserWithoutNickname() = Stream.of(Arguments.of(User(SOCIAL_TYPE, SOCIAL_USER_NUMBER)))

    @JvmStatic
    fun provideInvalidUserAndException() = Stream.of(
        Arguments.of(User(SOCIAL_TYPE, SOCIAL_USER_NUMBER, NICKNAME), IllegalStateException::class),
        Arguments.of(null, UserNotFoundException::class)
    )

    @JvmStatic
    fun provideInvalidNicknameRegisterRequest() = Stream.of(
        Arguments.of(NicknameRegisterRequest("ac/kr/smu/endticket/common/web")),
        Arguments.of(NicknameRegisterRequest("$^&@(a"))
    )

    @JvmStatic
    fun provideNicknameRegisterRequestAndException() = Stream.of(
        Arguments.of(NicknameRegisterRequest(NICKNAME),UserNotFoundException(USER_ID), 404 ),
        Arguments.of(NicknameRegisterRequest(NICKNAME),IllegalStateException(""), 409),
    )
}