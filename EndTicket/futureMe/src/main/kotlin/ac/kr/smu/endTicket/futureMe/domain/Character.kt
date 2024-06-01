package ac.kr.smu.endTicket.futureMe.domain

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

        // 캐릭터의 이미지 파일
        var image = ClassPathResource("characters/${name.lowercase()}.svg")
    }
}