package ac.kr.smu.endticket.futureme.event

import ac.kr.smu.endticket.common.web.enum.CharacterType
import ac.kr.smu.endticket.futureme.domain.futureme.model.FutureMe
import ac.kr.smu.endticket.futureme.imagination.USER_ID
import ac.kr.smu.endticket.futureme.ui.request.CreateFutureMeRequest

val FUTURE_ME = FutureMe.from(CreateFutureMeRequest(CharacterType.VEGA), USER_ID)