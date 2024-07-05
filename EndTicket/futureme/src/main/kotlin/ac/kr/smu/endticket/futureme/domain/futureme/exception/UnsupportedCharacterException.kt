package ac.kr.smu.endticket.futureme.domain.futureme.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * [Character.TYPE]의 변환을 실패했을 때 발생하는 에러
 * @param input 입력
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class UnsupportedCharacterException(
    val input: String,
) : IllegalArgumentException("$input 은 지원하지 않는 캐릭터입니다.")
