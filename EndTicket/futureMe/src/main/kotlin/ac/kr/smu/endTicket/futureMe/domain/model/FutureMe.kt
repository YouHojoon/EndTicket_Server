package ac.kr.smu.endTicket.futureMe.domain.model

import ac.kr.smu.endTicket.futureMe.ui.request.UpdateTitleOfFutureMeRequest
import jakarta.persistence.*

/**
 * 미래의 나를 추상화한 객체
 * @property userID 사용자 ID
 */
@Entity
@Table
class FutureMe(
    @Id
    val userID: Long
) {
    @Column(length = 13)
    var title: String = ""
        private set

    @Embedded
    val character: Character? = null

    @OneToMany(mappedBy = "imagination", cascade = [CascadeType.PERSIST, CascadeType.REMOVE], orphanRemoval = true)
    val imaginations: List<Imagination> = emptyList()

    /**
     * 제목을 업데이트 하는 메소드
     */
    fun updateTitle(request: UpdateTitleOfFutureMeRequest){
        this.title = request.title
    }
}