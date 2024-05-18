package ac.kr.smu.endTicket.test

import com.fasterxml.jackson.core.type.TypeReference

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.web.servlet.ResultActions
import java.lang.reflect.Type

//참고 : https://techblog.woowahan.com/14874/
inline fun <reified T> typeReference() = object: ParameterizedTypeReference<T>(){}
inline fun <reified T> ParameterizedTypeReference<T>.toJacksonTypeRef(): TypeReference<T>{
    val type: Type = this.type

    return object : TypeReference<T>() {
        override fun getType(): Type = type
    }
}

inline fun <reified T> ResultActions.andReturn(): T = ObjectMapper()
    .apply {
        //인자가 있는 생성자를 찾아주는 모듈
        registerModules(ParameterNamesModule())
    }
    .readValue(
        andReturn().response.getContentAsString(Charsets.UTF_8),
        typeReference<T>().toJacksonTypeRef()
    )
