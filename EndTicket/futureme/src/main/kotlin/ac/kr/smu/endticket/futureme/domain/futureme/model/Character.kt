package ac.kr.smu.endticket.futureme.domain.futureme.model

import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.futureme.domain.event.model.Event
import ac.kr.smu.endticket.futureme.domain.event.model.ImaginationCompletedEvent
import ac.kr.smu.endticket.futureme.domain.event.model.TicketCompletedEvent
import io.swagger.v3.oas.annotations.media.Schema
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
@Schema(description = "캐릭터")
class Character(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "캐릭터 종류", example = "VEGA")
    val type: CharacterType
) {
    companion object{
        private const val MAX_LEVEL = 40
        private const val MAX_EXPERIENCE_POINTS = 100
    }

    @Column
    @Schema(description = "레벨", example = "1", minimum = "0", maximum = "$MAX_LEVEL")
    var level: Int = 1
        private set

    @Column
    @Schema(description = "경험치", example = "100", minimum = "0", maximum = "$MAX_EXPERIENCE_POINTS")
    var experiencePoints: Int = 0
        private set

    /**
     * 이벤트를 받아 각 이벤트에 맞는 경험치를 상승한다.
     * 만약 경험치가 최대 경험치 이상이고, 레벨이 최대 레벨이 아니라면 레벨업한다.
     * @param event 발생한 이벤트
     */
    fun gainExperiencePoints(event: Event){
        when(event){
            is ImaginationCompletedEvent -> experiencePoints += 10
            is TicketCompletedEvent -> experiencePoints += 20
        }

        if (experiencePoints >= MAX_EXPERIENCE_POINTS)
            levelUpWhenLowerThanMaxLevel()
    }

    /**
     * 최대 레벨이 아니라면 레벨업한다.
     * 최대 레벨이라면 경험치를 최대 경험치로 고정한다.
     */
    private fun levelUpWhenLowerThanMaxLevel(){
        if (level < MAX_LEVEL){
            level++
            experiencePoints -= MAX_EXPERIENCE_POINTS
        }
        else
            experiencePoints = 100
    }

}