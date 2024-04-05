package ac.kr.smu.endTicket.user.domain.service

import ac.kr.smu.endTicket.user.domain.model.User
import ac.kr.smu.endTicket.user.domain.repository.UserRepository
import ac.kr.smu.protobuf.FindUserIDRequest
import ac.kr.smu.protobuf.UserIDResponse
import ac.kr.smu.protobuf.UserServiceGrpc
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.SQLIntegrityConstraintViolationException

/**
 * 사용자 관련 서비스 제공 클래스
 * @property userRepo 의존성 주입으로 얻는 user 저장소
 * @
 */
@Service
@GrpcService
class UserService(
    private val userRepo: UserRepository
): UserServiceGrpc.UserServiceImplBase() {

    /**
     * SNS 사용자 번호를 통해 해당 SNS의 사용자를 찾아 grpc를 통해 user id를 반환하는 메소드
     * @param socialType 해당 SNS로 회원가입한 사용자
     * @param socialUserNumber SNS 사용자 번호
     */
    @Transactional
    override fun findUserID(request: FindUserIDRequest, responseObserver: StreamObserver<UserIDResponse>) {
        val socialType = User.SocialType.valueOf(request.socialType.name)
        val id = userRepo.findBySocialTypeAndSocialUserNumber(socialType, request.socialUserNumber)?.id ?: userRepo.save(User(socialType, request.socialUserNumber)).id

        responseObserver.onNext(
            UserIDResponse.newBuilder().setUserId(id).build()
        )
        responseObserver.onCompleted()
    }



}