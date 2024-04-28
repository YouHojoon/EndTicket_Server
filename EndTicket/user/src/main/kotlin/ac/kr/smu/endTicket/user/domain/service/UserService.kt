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
import kotlin.jvm.optionals.getOrNull

/**
 * 사용자 관련 서비스 제공 클래스
 * @property userRepo 의존성 주입으로 얻는 user 저장소
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


    /**
     * 사용자의 닉네임 등록
     * @param userID 닉네임을 등록할 사용자
     * @param nickname 등록할 닉네임
     */
    @Transactional
    fun registerNickname(nickname: String, userID: Long){
        val user = userRepo.findById(userID).getOrNull()
        checkNotNull(user){"해당 userID의 사용자를 찾을 수 없습니다."}

        user.updateNickname(nickname)
    }
}