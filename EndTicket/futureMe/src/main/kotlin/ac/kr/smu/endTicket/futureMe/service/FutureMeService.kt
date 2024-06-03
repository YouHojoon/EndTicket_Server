package ac.kr.smu.endTicket.futureMe.service

import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.Character
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
import ac.kr.smu.endTicket.futureMe.domain.futureMe.repository.FutureMeRepository
import ac.kr.smu.endTicket.futureMe.ui.request.UpdateTitleOfFutureMeRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class FutureMeService(
    private val repo: FutureMeRepository
) {

    /**
     * 미래의 나를 생성하는 메소드
     * @param userID 생성을 요청하는 사용자
     * @param type 캐릭터 종류
     * @return 생성된 미래의 나
     */
    @Transactional
    fun createFutureMe(userID: Long, type: Character.Type) = repo.save(FutureMe(userID,type))

    @Transactional
    fun updateTitle(request: UpdateTitleOfFutureMeRequest, userID: Long): FutureMe{
        val futureMe = repo.findById(userID).getOrNull() ?: throw IllegalStateException("미래의 나가 존재하지 않습니다.")

        futureMe.updateTitle(request)
        return futureMe
    }
}