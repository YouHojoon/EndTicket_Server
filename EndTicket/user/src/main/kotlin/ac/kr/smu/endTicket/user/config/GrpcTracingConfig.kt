package ac.kr.smu.endTicket.user.config

import brave.Tracing
import brave.grpc.GrpcTracing
import io.grpc.ServerInterceptor

import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!test")
@Configuration(proxyBeanMethods = false)
class GrpcTracingConfig {

    @GrpcGlobalServerInterceptor
    fun tracingInterceptor(trace: Tracing): ServerInterceptor{
        return GrpcTracing.create(trace).newServerInterceptor()
    }
}