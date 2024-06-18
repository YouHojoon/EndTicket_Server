package ac.kr.smu.endTicket.auth

import ac.kr.smu.endTicket.protobuf.FindUserIDRequest
import ac.kr.smu.endTicket.protobuf.UserIDResponse
import ac.kr.smu.endTicket.protobuf.UserServiceGrpc
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
    GrpcClientAutoConfiguration::class
)
class GrpcConfiguration {
    @GrpcService
    class UserServiceImpl : UserServiceGrpc.UserServiceImplBase(){
        override fun findUserID(request: FindUserIDRequest, responseObserver: StreamObserver<UserIDResponse>) {
            if (request.socialUserNumber == "1") {
                responseObserver.onNext(
                    UserIDResponse.newBuilder().setUserID(UserServiceTest.USER_ID).build()
                )
                responseObserver.onCompleted()
            }
            else
                responseObserver.onError(RuntimeException())

        }
    }
}