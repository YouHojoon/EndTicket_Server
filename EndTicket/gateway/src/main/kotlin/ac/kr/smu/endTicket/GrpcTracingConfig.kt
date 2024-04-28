package ac.kr.smu.endTicket

import brave.Tracing
import brave.grpc.GrpcTracing
import io.grpc.ClientInterceptor
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class GrpcTracingConfig {
    @GrpcGlobalClientInterceptor
    fun tracingInterceptor(tracing:Tracing): ClientInterceptor{
        return GrpcTracing.create(tracing).newClientInterceptor()
    }
}