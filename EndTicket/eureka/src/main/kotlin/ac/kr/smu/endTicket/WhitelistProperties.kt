package ac.kr.smu.endTicket

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("whitelist")
data class WhitelistProperties(val addresses: List<String>)