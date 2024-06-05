package ac.kr.smu.endTicket.common.web.test

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.jetbrains.annotations.TestOnly
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.lang.reflect.Type
import ac.kr.smu.endTicket.response.BindExceptionResponse

//참고 : https://techblog.woowahan.com/14874/
inline fun <reified T> typeReference() = object: ParameterizedTypeReference<T>(){}
inline fun <reified T> ParameterizedTypeReference<T>.toJacksonTypeRef(): TypeReference<T>{
    val type: Type = this.type

    return object : TypeReference<T>() {
        override fun getType(): Type = type
    }
}

/**
 * [MockMvcResultHandlers] 의 Body를 역직렬화해 반환하는 메소드
 */
inline fun <reified T> ResultActions.andReturn(): T = ObjectMapper()
    .apply {
        //인자가 있는 생성자를 찾아주는 모듈
        registerModules(ParameterNamesModule())
    }
    .readValue(
        andReturn().response.getContentAsString(Charsets.UTF_8),
        typeReference<T>().toJacksonTypeRef()
    )


/**
 * 요청의 결과로 BindingException을 예상하는 메소드
 * @see BindExceptionResponse
 */
@TestOnly
fun ResultActions.expectBindingException(): ResultActions{
    return andExpect(MockMvcResultMatchers.status().isBadRequest)
        .andExpect(MockMvcResultMatchers.jsonPath("field").isString)
        .andExpect(MockMvcResultMatchers.jsonPath("code").value(400))
        .andExpect(MockMvcResultMatchers.jsonPath("message").isString)
        .andExpect(MockMvcResultMatchers.jsonPath("detail").isString)
}