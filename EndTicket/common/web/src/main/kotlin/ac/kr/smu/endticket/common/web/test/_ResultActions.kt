package ac.kr.smu.endticket.common.web.test

import ac.kr.smu.endticket.common.web.response.BindExceptionResponse
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.jetbrains.annotations.TestOnly
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.lang.reflect.Type

// 참고 : https://techblog.woowahan.com/14874/
@TestOnly
inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}

@TestOnly
inline fun <reified T> ParameterizedTypeReference<T>.toJacksonTypeRef(): TypeReference<T> {
    val type: Type = this.type

    return object : TypeReference<T>() {
        override fun getType(): Type = type
    }
}

/**
 * [MockMvcResultHandlers] 의 Body를 역직렬화해 반환하는 메소드
 * @return 역직렬화한 결과
 */
@TestOnly
inline fun <reified T> ResultActions.andReturn(): T =
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
 * @see ExceptionResponse
 * @return ExceptionResponse를 예상하는 ResultActions
 */
@TestOnly
fun ResultActions.expectExceptionResponse(): ResultActions =
    andExpect(MockMvcResultMatchers.jsonPath("code").isNumber)
        .andExpect(MockMvcResultMatchers.jsonPath("message").isString)
        .andExpect(MockMvcResultMatchers.jsonPath("detail").isString)

/**
 * 요청의 결과로 BindingException을 예상하는 메소드
 * @see BindExceptionResponse
 * @return BindindException을 예상하는 ResultActions
 */
@TestOnly
fun ResultActions.expectBindException(): ResultActions =
    andExpect(MockMvcResultMatchers.status().isBadRequest)
        .andExpect(MockMvcResultMatchers.jsonPath("field").isString)
        .andExpect(MockMvcResultMatchers.jsonPath("code").value(400))
        .andExpect(MockMvcResultMatchers.jsonPath("message").isString)
        .andExpect(MockMvcResultMatchers.jsonPath("detail").isString)
