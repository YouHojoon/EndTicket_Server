package ac.kr.smu.endTicket.futureMe.service

import ac.kr.smu.endTicket.futureMe.domain.event.model.Event
import ac.kr.smu.endTicket.futureMe.domain.futureMe.exception.NotFoundFutureMeException
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
    fun createFutureMe( type: Character.Type, userID: Long,) = repo.save(FutureMe(type,userID))

    /**
     * 미래의 나의 제목을 수정하는 메소드
     * @param request 수정 요청
     * @param userID 사용자 ID
     * @return 수정된 미래의 나
     * @throws NotFoundFutureMeException 미래의 나가 존재하지 않을 떄
     */
    @Transactional
    @Throws(NotFoundFutureMeException::class)
    fun updateTitle(request: UpdateTitleOfFutureMeRequest, userID: Long): FutureMe{
        val futureMe = repo.findById(userID).getOrNull() ?: throw NotFoundFutureMeException(userID)

        futureMe.updateTitle(request)
        return futureMe
    }

    /**
     * 미래의 나의 캐릭터를 수정하는 메소드
     * @param type 수정할 캐릭터 타입
     * @param userID 사용자 ID
     * @return 수정된 미래의 나
     * @throws NotFoundFutureMeException 미래의 나가 존재하지 않을 떄
     */
    @Transactional
    @Throws(NotFoundFutureMeException::class)
    fun changeCharacter(type: Character.Type, userID: Long): FutureMe{
        val futureMe = repo.findById(userID).getOrNull() ?: throw NotFoundFutureMeException(userID)

        futureMe.setCharacter(type, userID)
        return futureMe
    }

    /**
     * 각 이벤트에 해당되는 경헝치를 획득하는 메소드
     * @param event 발생된 이벤트
     * @throws NotFoundFutureMeException 상상해보기가 존재하지 않을 시
     */
    @Transactional
    fun gainExperiencePoints(event: Event){
        val futureMe = repo.findById(event.userID).getOrNull() ?: throw NotFoundFutureMeException(event.userID)
        futureMe.character.gainExperiencePoints(event)
    }

    /**
     * 미래의 나 조회
     * @param userID 사용자 ID
     * @return 조회된 미래의 나
     * @throws NotFoundFutureMeException 미래의 나가 존재하지 않을 시
     */
    @Transactional(readOnly = true)
    fun findFutureMe(userID: Long): FutureMe = repo.findById(userID).getOrNull() ?: throw NotFoundFutureMeException(userID)
}