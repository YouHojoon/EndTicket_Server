package ac.kr.smu.endticket.auth

import ac.kr.smu.endticket.protobuf.FindUserIdRequest
import ac.kr.smu.endticket.protobuf.UserIdResponse
import ac.kr.smu.endticket.protobuf.UserServiceGrpc
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration
import net.devh.boot.grpc.server.autoconfigure.GrpcServerAutoConfiguration
import net.devh.boot.grpc.server.autoconfigure.GrpcServerFactoryAutoConfiguration
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.context.annotation.Configuration

@Configuration
@ImportAutoConfiguration(
    GrpcServerAutoConfiguration::class,
    GrpcServerFactoryAutoConfiguration::class,
    GrpcClientAutoConfiguration::class,
)
class GrpcConfig {
    @GrpcService
    class UserServiceImpl : UserServiceGrpc.UserServiceImplBase() {
        override fun findUserId(
            request: FindUserIdRequest,
            responseObserver: StreamObserver<UserIdResponse>,
        ) {
            if (request.socialUserNumber == AuthTestParameters.SOCIAL_USER_NUMBER) {
                responseObserver.onNext(
                    UserIdResponse.newBuilder().setUserId(AuthTestParameters.USER_ID).build(),
                )
                responseObserver.onCompleted()
            } else {
                responseObserver.onError(RuntimeException())
            }
        }
    }
}
