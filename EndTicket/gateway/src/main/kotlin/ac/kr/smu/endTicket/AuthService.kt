package ac.kr.smu.endTicket

import ac.kr.smu.protobuf.AccessToken
import ac.kr.smu.protobuf.AuthServiceGrpc.AuthServiceBlockingStub
import ac.kr.smu.protobuf.ValidationResponse
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Service

@Service
class AuthService {
    @GrpcClient("auth")
    private lateinit var stub: AuthServiceBlockingStub

    fun validationToken(token: String): ValidationResponse{
        return stub.validationToken(AccessToken.newBuilder().setToken(token).build())
    }
}