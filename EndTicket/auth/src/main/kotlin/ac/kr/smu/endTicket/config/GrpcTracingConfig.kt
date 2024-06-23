package ac.kr.smu.endTicket.config

import brave.Tracing
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor
import org.springframework.context.annotation.Configuration
import brave.grpc.GrpcTracing
import io.grpc.ClientInterceptor
import io.grpc.ServerInterceptor
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import org.springframework.context.annotation.Profile

@Configuration(proxyBeanMethods = false)
class GrpcTracingConfig {
    @GrpcGlobalClientInterceptor
    fun tracingClientInterceptor(tracing: Tracing): ClientInterceptor{
        return GrpcTracing.create(tracing).newClientInterceptor()
    }

    @GrpcGlobalServerInterceptor
    fun tracingServerInterceptor(tracing: Tracing): ServerInterceptor{
        return GrpcTracing.create(tracing).newServerInterceptor()
    }

}