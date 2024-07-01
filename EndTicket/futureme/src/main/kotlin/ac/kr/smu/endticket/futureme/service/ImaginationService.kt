package ac.kr.smu.endticket.futureme.service

import ac.kr.smu.endticket.futureme.domain.event.model.ImaginationCompletedEvent
import ac.kr.smu.endticket.futureme.domain.imagination.exception.ImaginationNotFoundException
import ac.kr.smu.endticket.futureme.domain.imagination.exception.ImaginationOwnershipException
import ac.kr.smu.endticket.futureme.domain.imagination.model.Imagination
import ac.kr.smu.endticket.futureme.domain.imagination.repository.ImaginationRepository
import ac.kr.smu.endticket.futureme.ui.request.ImaginationRequest
import ac.kr.smu.endticket.futureme.ui.response.ImaginationResponse
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
     * @param userId 생성을 요청한 사용자의 id
     * @return 생성된 상상해보기
     * @throws IllegalStateException 최대 개수 이상으로 생성 시도할 시
     */
    @Transactional
    fun createImagination(request: ImaginationRequest, userId: Long): ImaginationResponse {
        check (repo.countByUserIdAndIsCompleteIsFalse(userId) < IMAGINATION_LIMIT){"$IMAGINATION_LIMIT 이상으로 상상해보기를 생성할 수 없습니다."}

        return repo.save(Imagination.from(request,userId)).toResponse()
    }

    /**
     * 상상해보기를 수정하는 메소드
     * @param request 수정 요청
     * @param id 상상해보기의 id
     * @param userId 수정을 요청한 사용자의 id
     * @return 수정된 상상해보기
     * @throws ImaginationNotFoundException id인 상상해보기가 존재하지 않을 시
     * @throws ImaginationOwnershipException 사용자가 상상해보기의 소유자가 아닐 시
     */
    @Transactional
    fun updateImagination(request: ImaginationRequest, id: Long, userId: Long): ImaginationResponse {
        val imagination = findById(id)

        imagination.update(request,userId)
        return imagination.toResponse()
    }

    /**
     * 상상해보기 완료 메소드, 완료를 성공하면 [ImaginationCompletedEvent]를 발행한다.
     * @param id 상상해보기 id
     * @param userId 사용자 id
     * @throws ImaginationNotFoundException id인 상상해보기가 존재하지 않을 시
     * @throws ImaginationOwnershipException 사용자가 상상해보기의 소유자가 아닐 시
     */
    @Transactional
    fun completeImagination(id: Long, userId: Long){
        val imagination = findById(id)

        imagination.complete(userId)
        futureMeEventService.publishEvent(ImaginationCompletedEvent(imagination))
    }

    /**
     * 상상해보기 조회 메소드
     * @param userId 사용자 id
     * @return 미완료된 상상해보기 반환
     */

    @Transactional(readOnly = true)
    fun findImaginations(userId: Long): Set<ImaginationResponse> =
        repo.findByUserIdAndIsCompleteIsFalse(userId)
            .map { it.toResponse() }
            .toSet()

    /**
     * 상상해보기 삭제
     * @param id 상상해보기 id
     * @param userId 사용자 id
     * @throws ImaginationNotFoundException id인 상상해보기가 존재하지 않을 시
     * @throws ImaginationOwnershipException 사용자가 상상해보기의 소유자가 아닐 시
     */
    @Transactional
    fun deleteImagination(id: Long, userId: Long){
        val imagination = findById(id)

        imagination.checkOwnership(userId)
        repo.delete(imagination)
    }

    /**
     * 상상해보기 id로 조회하는 메소드
     * @param id 상상해보기 id
     * @throws ImaginationNotFoundException id인 상상해보기가 존재하지 않을 시
     */
    private fun findById(id: Long) = repo.findById(id).orElseThrow { ImaginationNotFoundException(id) }
}