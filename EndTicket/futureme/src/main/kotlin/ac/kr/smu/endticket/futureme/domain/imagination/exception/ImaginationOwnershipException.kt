package ac.kr.smu.endticket.futureme.domain.imagination.exception

/**
 * 상상해보기의 소유자가 아닌 사용자가 요청했을 때 발생하는 에러
 * @property id 상상해보기 id
 * @property userId 요청한 사용자 id
 */
class ImaginationOwnershipException(
    val id: Long,
    val userId: Long
): RuntimeException("$userId 는 $id 의 소유자가 아닙니다.")