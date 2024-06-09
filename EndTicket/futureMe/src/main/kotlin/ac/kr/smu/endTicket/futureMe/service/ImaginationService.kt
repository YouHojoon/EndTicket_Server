package ac.kr.smu.endTicket.futureMe.service

import ac.kr.smu.endTicket.futureMe.domain.event.model.ImaginationCompletionEvent
import ac.kr.smu.endTicket.futureMe.domain.imagination.exception.NotFoundImaginationException
import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.domain.imagination.repository.ImaginationRepository
import ac.kr.smu.endTicket.futureMe.ui.request.ImaginationRequest
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Service
class ImaginationService(
    private val repo: ImaginationRepository,
    private val futureMeEventService: FutureMeEventService
) {
    companion object{
        const val IMAGINATION_LIMIT = 6
    }
    /**
     * 상상해보기를 생성하는 메소드
     * @param request 생성 요청
     * @param userID 생성을 요청한 사용자의 id
     * @return 생성된 상상해보기
     * @throws IllegalStateException 최대 개수 이상으로 생성 시도할 시
     */
    @Throws(IllegalStateException::class)
    fun createImagination(request: ImaginationRequest, userID: Long): ImaginationResponse{
        check (repo.countByUserIDAndIsCompleteIsFalse(userID) < IMAGINATION_LIMIT){"$IMAGINATION_LIMIT 이상으로 상상해보기를 생성할 수 없습니다."}
        return ImaginationResponse.from(repo.save(Imagination.from(request,userID)))
    }

    /**
     * 상상해보기를 수정하는 메소드
     * @param request 수정 요청
     * @param id 상상해보기의 id
     * @param userID 수정을 요청한 사용자의 id
     * @return 수정된 상상해보기
     * @throws NotFoundImaginationException 상상해보기가 존재하지 않을 시
     */
    @Throws(NotFoundImaginationException::class)
    fun updateImagination(request: ImaginationRequest, id: Long, userID: Long): ImaginationResponse{
        val imagination = repo.findById(id).getOrNull() ?: throw NotFoundImaginationException(id)

        imagination.update(request,userID)
        return ImaginationResponse.from(imagination)
    }

    /**
     * 상상해보기 완료 메소드, 완료를 성공하면 [ImaginationCompletionEvent]를 발행한다.
     * @param id 상상해보기 id
     * @param userID 사용자 id
     * @throws NotFoundImaginationException 상상해보기가 존재하지 않을 시
     */
    @Transactional
    fun completeImagination(id: Long, userID: Long){
        val imagination = repo.findById(id).getOrNull() ?: throw NotFoundImaginationException(id)

        imagination.complete(userID)
        futureMeEventService.eventPublish(ImaginationCompletionEvent(imagination))
    }

    /**
     * 상상해보기 조회 메소드
     * @param userID 사용자 id
     * @return 미완료된 상상해보기 반환
     */

    @Transactional(readOnly = true)
    fun findImaginations(userID: Long): Set<ImaginationResponse> =
        repo.findByUserIDAndIsCompleteIsFalse(userID)
            .map { ImaginationResponse.from(it) }
            .toSet()

    /**
     * 상상해보기 삭제
     * @param id 상상해보기 id
     * @param userID 사용자 id
     * @throws NotFoundImaginationException 상상해보기가 존재하지 않을 시
     */
    @Transactional
    fun deleteImagination(id: Long, userID: Long){
        val imagination = repo.findById(id).getOrNull() ?: throw NotFoundImaginationException(id)

        imagination.checkOwnership(userID)
        repo.delete(imagination)
    }
}