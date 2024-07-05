package ac.kr.smu.endticket.common.web

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class EnableAutoAsyncConfigTest {
    @Test
    fun get_enableAutoAsyncConfig_then_importAsyncConfigWithCorePoolSize() {
        ApplicationContextRunner()
            .withBean(MockBean::class.java)
            .run {
                Assertions
                    .assertThat(it)
                    .hasBean("asyncConfig")
            }
    }
}
