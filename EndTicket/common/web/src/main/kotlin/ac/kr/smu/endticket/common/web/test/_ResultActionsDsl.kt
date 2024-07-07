package ac.kr.smu.endticket.common.web.test

import ac.kr.smu.endticket.common.web.response.BindExceptionResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.jetbrains.annotations.TestOnly
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.ResultActionsDsl

/**
 * [MockHttpServletResponse] 의 Body를 역직렬화해 반환하는 메소드
 * @return 역직렬화한 결과
 */
inline fun <reified T> ResultActionsDsl.andReturn(): T =
    ObjectMapper()
        .apply {
            // 인자가 있는 생성자를 찾아주는 모듈
            registerModules(ParameterNamesModule())
        }.readValue(
            andReturn().response.getContentAsString(Charsets.UTF_8),
            typeReference<T>().toJacksonTypeRef(),
        )

/**
 * 요청의 응답으로 [ExceptionResponse]을 예상하는 메소드
 * @param status 예상하는 응답 상태 코드
 * @see ExceptionResponse
 * @return ExceptionResponse를 예상하는 ResultActions
 */
@TestOnly
fun ResultActionsDsl.expectExceptionResponse(status: HttpStatus) =
    andExpect {
        status {
            isEqualTo(status.value())
        }
        jsonPath("code") {
            value(status.value())
        }
        jsonPath("message") {
            isString()
        }
        jsonPath("detail") {
            isString()
        }
    }

/**
 * 요청의 결과로 BindingException을 예상하는 메소드
 * @see BindExceptionResponse
 * @return BindindException을 예상하는 ResultActions
 */
@TestOnly
fun ResultActionsDsl.expectBindExceptionResponse() =
    expectExceptionResponse(HttpStatus.BAD_REQUEST)
        .andExpect {
            jsonPath("field") {
                isString()
            }
        }
