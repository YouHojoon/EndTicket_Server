package ac.kr.smu.endTicket.infra.oAuth.IDToken

class IDTokenPayLoad{
    var iss: String = ""
    var aud: String = ""
    var sub: Long = 0
    var iat: String = ""
    var auth_time: Long = 0
    var exp: Long = 0
    var nonce: String? = null
    var email: String? = null
}