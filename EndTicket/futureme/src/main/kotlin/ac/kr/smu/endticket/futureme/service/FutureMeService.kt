package ac.kr.smu.endticket.futureme.service

import ac.kr.smu.endticket.futureme.ui.request.CreateFutureMeRequest
import ac.kr.smu.endticket.futureme.domain.event.model.Event
import ac.kr.smu.endticket.futureme.domain.futureme.exception.FutureMeNotFoundException
import ac.kr.smu.endticket.futureme.domain.futureme.model.FutureMe
import ac.kr.smu.endticket.futureme.domain.futureme.repository.FutureMeRepository
import ac.kr.smu.endticket.futureme.ui.request.UpdateFutureMeRequest
import ac.kr.smu.endticket.futureme.ui.response.FutureMeResponse
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
     * @param userId 생성을 요청하는 사용자
     * @return 생성된 미래의 나
     */
    @Transactional
    fun createFutureMe(request: CreateFutureMeRequest, userId: Long) =
        if (repo.existsById(userId))
            throw IllegalStateException("미래의 나가 이미 존재합니다.")
        else
            repo.save(FutureMe.from(request,userId)).toResponse()

    /**
     * 미래의 나를 수정하는 메소드
     * @param request 수정 요청
     * @param userId 사용자 Id
     * @return 수정된 미래의 나
     * @throws FutureMeNotFoundException 미래의 나가 존재하지 않을 떄
     */
    @Transactional
    @Throws(FutureMeNotFoundException::class)
    fun updateFutureMe(request: UpdateFutureMeRequest, userId: Long): FutureMeResponse {
        val futureMe = repo.findById(userId).getOrNull() ?: throw FutureMeNotFoundException(userId)

        futureMe.update(request)

        return futureMe.toResponse()
    }

    /**
     * 각 이벤트에 해당되는 경헝치를 획득하는 메소드
     * @param event 발생된 이벤트
     * @throws FutureMeNotFoundException 상상해보기가 존재하지 않을 시
     */
    @Transactional
    fun gainExperiencePoints(event: Event){
        val futureMe = repo.findById(event.userId).getOrNull() ?: throw FutureMeNotFoundException(event.userId)
        futureMe.gainExperiencePoints(event)
    }

    /**
     * 미래의 나 조회
     * @param userId 사용자 Id
     * @return 조회된 미래의 나
     * @throws FutureMeNotFoundException 미래의 나가 존재하지 않을 시
     */
    @Transactional(readOnly = true)
    fun findFutureMe(userId: Long): FutureMeResponse {
        val futureMe = repo.findById(userId).getOrNull() ?: throw FutureMeNotFoundException(userId)

        return futureMe.toResponse()
    }

    @Transactional
    fun deleteFutureMe(userId: Long){
        repo.deleteById(userId)
    }
}