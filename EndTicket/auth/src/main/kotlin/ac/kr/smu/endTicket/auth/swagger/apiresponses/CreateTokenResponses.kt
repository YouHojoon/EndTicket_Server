package ac.kr.smu.endTicket.auth.swagger.apiresponses

import ac.kr.smu.endTicket.auth.ui.response.TokenResponse
import ac.kr.smu.endticket.common.web.response.ExceptionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses

@Operation(
    summary = "SNS를 사용해 토큰 생성",
    description = "SNS 로그인으로 발급받은 authorization code를 이용해 Access 토큰과 Refresh 토큰 생성<br>회원 정보가 없을 시 가입 요청한다.",
)
@ApiResponses(
    value = [
        ApiResponse(
            description = "인증 성공",
            responseCode = "200",
            content = [Content(schema = Schema(implementation = TokenResponse::class))],
        ),
        ApiResponse(
            description = "잘못된 인증 코드",
            responseCode = "400",
            content = [Content(schema = Schema(implementation = ExceptionResponse::class))],
        ),
        ApiResponse(
            description = "시용자 서버 통신 실패",
            responseCode = "503",
            content = [Content(schema = Schema(implementation = ExceptionResponse::class))],
        ),
    ],
)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CreateTokenResponses
