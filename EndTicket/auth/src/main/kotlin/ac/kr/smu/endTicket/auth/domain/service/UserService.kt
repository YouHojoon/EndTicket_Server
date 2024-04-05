package ac.kr.smu.endTicket.auth.domain.service


import ac.kr.smu.endTicket.auth.domain.model.SocialType
import ac.kr.smu.protobuf.FindUserIDRequest
import ac.kr.smu.protobuf.UserServiceGrpc
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Service

/**
 * User 서버와 gRPC를 통해 통신하는 객체
 */
@Service
class UserService{
    @GrpcClient("user")
    private lateinit var userStub: UserServiceGrpc.UserServiceBlockingStub

    /**
     * 사용자 번호를 반환하는 메소드, 만약 가입이 되어 있지 않다면 가입된다.
     * @param socialType 가입된 SNS 종류
     * @param socialUserNumber 해당 SNS의 사용자 번호
     * @return 사용자 번호
     */
    fun findUserID(socialType: SocialType, socialUserNumber: String): Long{
        return userStub.findUserID(
            FindUserIDRequest.newBuilder()
                .setSocialType(ac.kr.smu.protobuf.SocialType.valueOf(socialType.name))
                .setSocialUserNumber(socialUserNumber)
                .build()
        ).userId
    }

}