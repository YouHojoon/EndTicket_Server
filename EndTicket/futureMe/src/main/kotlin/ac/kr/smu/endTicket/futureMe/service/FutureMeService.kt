package ac.kr.smu.endTicket.futureMe.service

import ac.kr.smu.endTicket.futureMe.domain.event.model.Event
import ac.kr.smu.endTicket.futureMe.domain.futureMe.exception.FutureMeNotFoundException
import ac.kr.smu.endTicket.futureMe.domain.futureMe.model.FutureMe
import ac.kr.smu.endTicket.futureMe.domain.futureMe.repository.FutureMeRepository
import ac.kr.smu.endTicket.futureMe.ui.request.CreateFutureMeRequest
import ac.kr.smu.endTicket.futureMe.ui.request.UpdateFutureMeRequest
import ac.kr.smu.endTicket.futureMe.ui.response.FutureMeResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class FutureMeService(
    private val repo: FutureMeRepository
) {

    /**
     * 미래의 나를 생성하는 메소드
     * @param request 생성 요청
     * @param userID 생성을 요청하는 사용자
     * @return 생성된 미래의 나
     */
    @Transactional
    fun createFutureMe(request: CreateFutureMeRequest, userID: Long) = if (repo.existsById(userID)) throw IllegalStateException("미래의 나가 이미 존재합니다.") else FutureMeResponse.from(repo.save(FutureMe.from(request,userID)))

    /**
     * 미래의 나를 수정하는 메소드
     * @param request 수정 요청
     * @param userID 사용자 ID
     * @return 수정된 미래의 나
     * @throws FutureMeNotFoundException 미래의 나가 존재하지 않을 떄
     */
    @Transactional
    @Throws(FutureMeNotFoundException::class)
    fun update(request: UpdateFutureMeRequest, userID: Long): FutureMeResponse{
        val futureMe = repo.findById(userID).getOrNull() ?: throw FutureMeNotFoundException(userID)

        futureMe.update(request)

        return FutureMeResponse.from(futureMe)
    }

    /**
     * 각 이벤트에 해당되는 경헝치를 획득하는 메소드
     * @param event 발생된 이벤트
     * @throws FutureMeNotFoundException 상상해보기가 존재하지 않을 시
     */
    @Transactional
    fun gainExperiencePoints(event: Event){
        val futureMe = repo.findById(event.userID).getOrNull() ?: throw FutureMeNotFoundException(event.userID)
        futureMe.character.gainExperiencePoints(event)
    }

    /**
     * 미래의 나 조회
     * @param userID 사용자 ID
     * @return 조회된 미래의 나
     * @throws FutureMeNotFoundException 미래의 나가 존재하지 않을 시
     */
    @Transactional(readOnly = true)
    fun findFutureMe(userID: Long): FutureMeResponse{
        val futureMe = repo.findById(userID).getOrNull() ?: throw FutureMeNotFoundException(userID)

        return FutureMeResponse.from(futureMe)
    }
}