package ac.kr.smu.endticket.common.jpa

import org.springframework.data.jpa.repository.JpaRepository

fun <T:Any, ID: Any> JpaRepository<T, ID>.findByIdOrThrow(id: ID) = findById(id).orElseThrow { EntityNotFoundException(id) }
