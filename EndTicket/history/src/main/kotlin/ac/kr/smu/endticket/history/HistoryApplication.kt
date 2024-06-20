package ac.kr.smu.endticket.history

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing


@SpringBootApplication
@EnableJpaAuditing
class HistoryApplication
fun main(args: Array<String>){
    runApplication<HistoryApplication>(*args)
}