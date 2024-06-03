package ac.kr.smu.endTicket.futureMe.service

import ac.kr.smu.endTicket.futureMe.domain.imagination.exception.NotFoundImaginationException
import ac.kr.smu.endTicket.futureMe.domain.imagination.model.Imagination
import ac.kr.smu.endTicket.futureMe.domain.imagination.repository.ImaginationRepository
import ac.kr.smu.endTicket.futureMe.ui.request.ImaginationRequest
import ac.kr.smu.endTicket.futureMe.ui.response.ImaginationResponse
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class ImaginationService(
    private val repo: ImaginationRepository
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
    fun createImagination(request: ImaginationRequest, userID: Long){
        check (repo.countById(userID) < IMAGINATION_LIMIT){"$IMAGINATION_LIMIT 이상으로 상상해보기를 생성할 수 없습니다."}
        ImaginationResponse.from(repo.save(Imagination.from(request,userID)))
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
}