package ac.kr.smu.endticket.futureme.imagination

import ac.kr.smu.endticket.futureme.domain.imagination.model.Imagination
import ac.kr.smu.endticket.futureme.ui.request.ImaginationRequest

const val USER_ID = 1L
val request = ImaginationRequest(
    "b",
    "t",
    Imagination.Color.BLUE1
)



val invalidBehaviorRequest = ImaginationRequest(
    behavior = "new behavior",
    target = "target",
    color = Imagination.Color.GRAY2
)
val invalidTargetRequest = ImaginationRequest(
    behavior = "behavior",
    target = "new target with exceed",
    color = Imagination.Color.GRAY2
)