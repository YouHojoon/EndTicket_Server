package ac.kr.smu.endticket.eureka

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("whitelist")
data class WhitelistProperties(val addresses: List<String>)