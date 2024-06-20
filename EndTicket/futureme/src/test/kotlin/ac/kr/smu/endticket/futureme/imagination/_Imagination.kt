package ac.kr.smu.endticket.futureme.imagination

import ac.kr.smu.endticket.common.web.enum.Color
import ac.kr.smu.endticket.futureme.ui.request.ImaginationRequest


const val USER_ID = 1L
val request = ImaginationRequest(
    "b",
    "t",
    Color.BLUE1
)

val invalidBehaviorRequest = ImaginationRequest(
    behavior = "new behavior",
    target = "target",
    color = Color.GRAY2
)
val invalidTargetRequest = ImaginationRequest(
    behavior = "behavior",
    target = "new target with exceed",
    color = Color.GRAY2
)