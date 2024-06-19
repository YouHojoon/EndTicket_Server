package ac.kr.smu.endticket.user

import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration

@ImportAutoConfiguration(
    GrpcServerFactoryAutoConfiguration::class,
    GrpcServerAutoConfiguration::class,
    GrpcClientAutoConfiguration::class
)
@TestConfiguration
class GrpcConfig