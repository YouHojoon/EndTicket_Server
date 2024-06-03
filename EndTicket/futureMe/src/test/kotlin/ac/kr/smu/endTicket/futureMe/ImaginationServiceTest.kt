package ac.kr.smu.endTicket.futureMe

import ac.kr.smu.endTicket.futureMe.domain.imagination.repository.ImaginationRepository
import ac.kr.smu.endTicket.futureMe.service.ImaginationService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class ImaginationServiceTest(
    @Mock
    private val repo: ImaginationRepository
) {
    @InjectMocks
    private lateinit var service: ImaginationService

    @BeforeEach
    fun init() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    @DisplayName("제한 개수 이상으로 상상해보기 생성 테스트")
    fun given_requestMoreThanImaginationLimit_when_createImagination_then_throwIllegalStateException(){
        Mockito.`when`(repo.countById(USER_ID))
            .thenReturn(6)

        assertThrows<IllegalStateException> {  service.createImagination(request, USER_ID)}
    }
}