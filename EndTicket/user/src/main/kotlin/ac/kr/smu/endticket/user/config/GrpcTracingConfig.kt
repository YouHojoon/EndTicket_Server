package ac.kr.smu.endticket.user.config

import brave.Tracing
import brave.grpc.GrpcTracing
import io.grpc.ServerInterceptor
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class GrpcTracingConfig {
    @GrpcGlobalServerInterceptor
    fun tracingInterceptor(trace: Tracing): ServerInterceptor = GrpcTracing.create(trace).newServerInterceptor()
}
