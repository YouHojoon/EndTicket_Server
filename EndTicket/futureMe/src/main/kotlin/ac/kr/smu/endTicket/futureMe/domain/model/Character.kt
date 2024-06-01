package ac.kr.smu.endTicket.futureMe.domain.model

import ac.kr.smu.endTicket.futureMe.domain.model.event.Event
import ac.kr.smu.endTicket.futureMe.domain.model.event.FutureMeCompletionEvent
import ac.kr.smu.endTicket.futureMe.domain.model.event.TicketCompletionEvent
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import org.springframework.core.io.ClassPathResource


/**
 * 캐릭터에 관한 정보를 저장하는 객체
 * @property type 캐릭터의 종류
 */
@Embeddable
class Character(
    type: Type
) {
    companion object{
        private val MIN_LEVEL = 1
        private val MAX_LEVEL = 40
        private val MIN_EXPERIENCE_POINTS = 0
        private val MAX_EXPERIENCE_POINTS = 100
    }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: Type
        private set

    init {
        this.type = type
    }

    @Column
    var level: Int = 1
        private set

    @Column
    var experiencePoints: Int = 0
        private set

    /**
     * 캐릭터의 종류
     */
    enum class Type{
        KIA, CHEESE, VEGA;

        // 캐릭터의 이미지 파목
        var imageResource = ClassPathResource("characters/${name.lowercase()}.svg")
    }

    /**
     * 이벤트를 받아 각 이벤트에 맞는 경험치를 상승한다.
     * 만약 경험치가 최대 경험치 이상이고, 레벨이 최대 레벨이 아니라면 레벨업한다.
     * @param event 발생한 이벤트
     */
    fun gainExperiencePoints(event: Event){
        when(event){
            is FutureMeCompletionEvent -> experiencePoints += 10
            is TicketCompletionEvent -> experiencePoints += 20
        }

        if (experiencePoints >= MAX_EXPERIENCE_POINTS)
            levelUpWhenLowerThanMaxLevel()

    }

    /**
     * 캐릭터의 타입을 업데이트한다. 업데이트를 수행하면 레벨과 경험치는 초기화된다.
     * @param type 변경할 타입
     */
    fun updateType(type: Type){
        this.type = type
        this.level = MIN_LEVEL
        this.experiencePoints = MIN_EXPERIENCE_POINTS
    }

    private fun levelUpWhenLowerThanMaxLevel(){
        if (level < MAX_LEVEL){
            level++
            experiencePoints = 0
        }
        else
            experiencePoints = 100
    }

}