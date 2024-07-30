package ac.kr.smu.endticket.auth.config

import brave.Tracing
import brave.grpc.GrpcTracing
import io.grpc.ClientInterceptor
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class GrpcTracingConfig {
    @GrpcGlobalClientInterceptor
    fun tracingClientInterceptor(tracing: Tracing): ClientInterceptor = GrpcTracing.create(tracing).newClientInterceptor()
}
