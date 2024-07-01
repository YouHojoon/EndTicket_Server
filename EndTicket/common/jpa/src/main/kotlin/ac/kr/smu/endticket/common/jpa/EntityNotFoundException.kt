package ac.kr.smu.endticket.common.jpa

import java.io.Serializable

class EntityNotFoundException(id: Serializable): RuntimeException("$id 의 ")