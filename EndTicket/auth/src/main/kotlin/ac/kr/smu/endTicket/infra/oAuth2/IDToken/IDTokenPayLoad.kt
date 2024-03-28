package ac.kr.smu.endTicket.infra.oAuth2.IDToken

class IDTokenPayLoad{
    var iss: String = ""
    var aud: String = ""
    var sub: String = ""
    var exp: Long = 0
    var iat: Long = 0

    var nonce: String? = null
    var email: String? = null
    var auth_time: Long? = null
    var azp: String? = null
    var email_verified: Boolean? = null
    var at_hash: String? = null
}