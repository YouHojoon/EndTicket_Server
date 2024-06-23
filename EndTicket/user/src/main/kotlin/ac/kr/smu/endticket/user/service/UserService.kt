package ac.kr.smu.endticket.user.service

import ac.kr.smu.endticket.protobuf.FindUserIdRequest
import ac.kr.smu.endticket.protobuf.UserIdResponse
import ac.kr.smu.endticket.protobuf.UserServiceGrpc
import ac.kr.smu.endticket.user.domain.exception.UserNotFoundException
import ac.kr.smu.endticket.user.domain.model.User
import ac.kr.smu.endticket.user.domain.repository.UserRepository
import ac.kr.smu.endticket.user.ui.request.NicknameRegisterRequest
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

/**
 * 사용자 관련 서비스 제공 클래스
 * @property userRepo 의존성 주입으로 얻는 user 저장소
 */
@Service
@GrpcService
class UserService(
    private val repo: UserRepository
): UserServiceGrpc.UserServiceImplBase() {

    /**
     * SNS 사용자 번호로 해당 SNS의 사용자를 찾아 grpc를 통해 user id를 반환하는 메소드, 만약에 없다면 저장한다.
     * @param socialType 해당 SNS로 회원가입한 사용자
     * @param socialUserNumber SNS 사용자 번호
     */
    @Transactional
    override fun findUserId(request: FindUserIdRequest, responseObserver: StreamObserver<UserIdResponse>) {
        val socialType = User.SocialType.valueOf(request.socialType.name)
        val id = repo.findBySocialTypeAndSocialUserNumber(socialType, request.socialUserNumber)?.id ?:
        repo.save(
            User(socialType, request.socialUserNumber)
        ).id

        responseObserver.onNext(
            UserIdResponse.newBuilder().setUserId(id).build()
        )
        responseObserver.onCompleted()
    }


    /**
     * 사용자의 닉네임 등록
     * @param request 닉네임 등록 요청
     * @param id 닉네임을 등록할 사용자
     * @throws UserNotFoundException id의 사용자가 없을 시
     */
    @Throws(UserNotFoundException::class)
    @Transactional
    fun registerNickname(request: NicknameRegisterRequest, id: Long){
        val user = repo.findUser(id)
        
        user.registerNickname(request)
    }

    /**
     * 닉네임 조회 메소드
     * @param id 사용자 id
     * @throws UserNotFoundException id 인 사용자가 존재하지 않을 시
     */
    @Transactional(readOnly = true)
    @Throws(UserNotFoundException::class)
    fun findNickname(id:Long): String? = repo.findUser(id).nickname


    /**
     * 사용자를 조회 메소드
     * @param id 사용자 id
     * @throws UserNotFoundException id인 사용자가 존재하지 않을 시
     */
    @Throws(UserNotFoundException::class)
    private fun UserRepository.findUser(id: Long) = findById(id).getOrNull() ?: throw UserNotFoundException(id)
}