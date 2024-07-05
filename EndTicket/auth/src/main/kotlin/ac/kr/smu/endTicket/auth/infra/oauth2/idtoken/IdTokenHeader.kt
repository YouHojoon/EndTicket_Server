package ac.kr.smu.endTicket.auth.infra.oauth2.idtoken

/**
 * Id 토큰의 헤더를 추상화한 클래스
 * @property kid key의 ID
 * @property typ ID 토큰의 종류
 * @property alg 서명이 암호화된 알고리즘
 */
data class IdTokenHeader(
    var kid: String = "",
    var typ: String = "",
    var alg: String = "",
)
