package com.foodmate.backend.service;

import com.foodmate.backend.component.MailComponents;
import com.foodmate.backend.dto.MemberDto;
import com.foodmate.backend.entity.Food;
import com.foodmate.backend.entity.Likes;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.entity.Preference;
import com.foodmate.backend.enums.EmailContents;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.enums.MemberLoginType;
import com.foodmate.backend.exception.FileException;
import com.foodmate.backend.exception.FoodException;
import com.foodmate.backend.exception.MemberException;
import com.foodmate.backend.repository.FoodRepository;
import com.foodmate.backend.repository.LikesRepository;
import com.foodmate.backend.repository.MemberRepository;
import com.foodmate.backend.repository.PreferenceRepository;
import com.foodmate.backend.security.dto.JwtTokenDto;
import com.foodmate.backend.security.service.JwtTokenProvider;
import com.foodmate.backend.util.RandomStringMaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final PreferenceRepository preferenceRepository;
    private final FoodRepository foodRepository;
    private final LikesRepository likesRepository;
    private final S3Uploader s3Uploader;
    private final S3Deleter s3Deleter;
    private final String s3BucketFolderName = "profile-images/";
    @Value("${S3_GENERAL_IMAGE_PATH}")
    private String defaultProfileImage;
    private final MailComponents mailComponents;
    private final JwtTokenProvider jwtTokenProvider;



    /**
     * @param authentication 로그인한 사용자의 정보
     * @return 사용자의 정보
     */
    public MemberDto.myMemberInfoResponse getMemberInfo(Authentication authentication) {
        /* 사용자가 없을 시 예외 처리 */
        Member member = memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));

        if(member.getImage() == null){
            return MemberDto.myMemberInfoResponse.createMemberDtoResponse(member, findPreferences(member) , defaultProfileImage);
        }
        return MemberDto.myMemberInfoResponse.createMemberDtoResponse(member, findPreferences(member));
    }

    /**
     * @param authentication
     * @param imageFile
     * 사용자에게 사진파일을 받아와 프로필 이미지 변경
     */
    @Transactional
    public void patchProfileImage(Authentication authentication, MultipartFile imageFile) {
        /* 사용자가 없을 시 예외 처리 */
        Member member = memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));

       /* 기존 이미지가 null이 아니면,
       있던 프로필 정보를 삭제하기 위한 s3 삭제 */
        if (isProfileImage(member.getImage())) {
            try {
                s3Deleter.deleteObject(getImageObjectKey(member.getImage()));
            } catch (IOException e) {
                throw new FileException(Error.DELETE_IMAGE_FILE_FAILED);
            }
        }

        /* 새로 받아온 사진을 UUID를 사용한 무작위의 파일명으로 변경 후 s3업로드 */
        uploadProfileImage(member, imageFile);
    }



    /**
     *
     * @param memberImage
     * @return 기존에 이미지가 있으면 true 없으면 false
     *
     */
    private boolean isProfileImage(String memberImage){
        if(memberImage == null) {
            return false;
        }
        return true;
    }

    /**
     * @param imageUrl
     * @return s3에 있는 objectKey를 찾기위한 메서드
     */
    private String getImageObjectKey(String imageUrl){
        StringTokenizer st = new StringTokenizer(imageUrl,"/");
        StringBuilder objectKey = new StringBuilder(s3BucketFolderName);
        String str = "";
        while(st.hasMoreTokens()){
            str = st.nextToken();
        }
        objectKey.append(str);
        return objectKey.toString();
    }

    /**
     * 파일럽로드 메서드
     * @param member
     * @param imageFile
     * @throws IOException
     */
    private void uploadProfileImage(Member member, MultipartFile imageFile){
        try{
        member.setImage(
                s3Uploader.uploadAndGenerateUrl(
                        imageFile,
                        s3BucketFolderName +
                                RandomStringMaker.randomStringMaker())
        );
        } catch (IOException e){
            throw new FileException(Error.UPLOAD_IMAGE_FILE_FAILED);
        }
        memberRepository.save(member);
    }

    /**
     * @param email
     * @return 현재 사용중인 email 이면 false 리턴
     *         아니면 true
     */
    public Boolean checkDuplicateEmail(String email) {
        if(memberRepository.findByEmail(email).isPresent()){
            return false;
        }
        return true;
    }

    /**
     * @param nickname
     * @return 현재 사용중인 nickname 이면 false 리턴
     *         아니면 true
     */
    public Boolean checkDuplicateNickname(String nickname) {
        if(memberRepository.findByNickname(nickname).isPresent()){
            return false;
        }
        return true;
    }

    /**
     * 로그 아웃 기능
     * @param request
     * @param response
     * @return 로그아웃 상태 호출
     */
    public void logoutMember(HttpServletRequest request, HttpServletResponse response) {
        logout(request, response);
    }

    /**
     * @param request
     * @param response
     * 로그아웃 로직 구현
     */
    private void logout(HttpServletRequest request, HttpServletResponse response){
        // 현재 사용자의 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 로그아웃 처리
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, authentication);
    }

    /**
     * @param nickname
     * @return 입력받은 nickname 회원 조회
     */
    public MemberDto.otherMemberInfoResponse getMemberInfoByNickname(String nickname, Authentication authentication) {
        Member loginMember = memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));

        Member otherMember = memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));

        if(otherMember.getIsDeleted() != null){ // 정지 당한 유저
            throw new MemberException(Error.DELETED_USER);
        }

        boolean likeStatus = false;

        Optional<Likes> optionalLikes = likesRepository.findByLikedAndLiker(otherMember, loginMember);

        if(optionalLikes.isPresent()) {
            likeStatus = true;
        }

        if(otherMember.getImage() == null){
            return MemberDto.otherMemberInfoResponse.createMemberDtoResponse(otherMember, findPreferences(otherMember) , defaultProfileImage, likeStatus);
        }
        return MemberDto.otherMemberInfoResponse.createMemberDtoResponse(otherMember, findPreferences(otherMember), likeStatus);
    }

    /**
     * 유저의 선호음식 찾는 메서드
     * @param member
     * @return 선호음식의 종류 리스트
     */
    private List<String> findPreferences(Member member){
        List<String> foods = new ArrayList<>();
        List<Preference> preferences = preferenceRepository.findAllByMember(member);
        for (Preference preference : preferences){
            Food food = foodRepository.findById(preference.getFood().getId()).orElseThrow(
                    () -> new FoodException(Error.FOOD_NOT_FOUND)
            );
            foods.add(food.getType());
        }
        return foods;

    }

    /**
     * 회원가입 진행하면서 이메일, 닉네임 중복 확인 진행하므로 추가 중복 확인 x
     */
    @Transactional
    public void createMember(MemberDto.CreateMemberRequest request, MultipartFile imageFile)  {
        String uuid = RandomStringMaker.randomStringMaker();

        Member member = Member.createGeneralMember(
                request,
                BCrypt.hashpw(request.getPassword(),
                        BCrypt.gensalt()),
                uuid);
        memberRepository.save(member);
        processFoodPreferences(member, request.getFood()); // 선호음식 등록
        uploadProfileImage(member, imageFile); // 사진 업로드
        sendMailEmailKey(request.getEmail(), uuid); // 메일 전송
    }

    @Transactional
    public void createDefaultImageMember(MemberDto.CreateMemberRequest request) {
        String uuid = UUID.randomUUID().toString();
        Member member = Member.createGeneralMember(
                request,
                BCrypt.hashpw(request.getPassword(),
                        BCrypt.gensalt()),
                uuid);
        memberRepository.save(member);
        processFoodPreferences(member, request.getFood()); // 선호음식 등록
        sendMailEmailKey(request.getEmail(), uuid); // 메일 전송
    }

    public boolean emailAuth(String emailAuthKey) {

        Optional<Member> optionalMember = memberRepository.findByEmailAuthKey(emailAuthKey);
        if (!optionalMember.isPresent()) {
            return false;
        }
        Member member = optionalMember.get();

        // 계정 반복 활성화 방지
        if (member.getIsEmailAuth()) {
            return false;
        }
        member.setIsEmailAuth(true);
        member.setEmailAuthDate(LocalDateTime.now());
        memberRepository.save(member);

        return true;
    }

    /**
     * 인증 메일 전송 메서드
     * @param email
     * @param uuid
     */
    private void sendMailEmailKey(String email, String uuid){
        mailComponents.sendMail(
                email,
                EmailContents.WELCOME.getSubject(),
                EmailContents.WELCOME.getText().replace("{uuid}", uuid)
        );
    }

    private void sendMailResetPassword(String email, String uuid){
        mailComponents.sendMail(
                email,
                EmailContents.RESET_PASSWORD.getSubject(),
                EmailContents.RESET_PASSWORD.getText().replace("{uuid}", uuid)
        );
    }

    /**
     * 선호음식 넣는 메서드
     * @param member
     * @param foodNames
     */
    private void processFoodPreferences(Member member, List<String> foodNames){
        if (foodNames != null && !foodNames.isEmpty()) {
            for (String foodName : foodNames) {
                Food food = foodRepository.findByType(foodName) // 음식 이름으로 음식 엔티티 찾기
                        .orElseThrow(() -> new FoodException(Error.FOOD_NOT_FOUND));
                Preference preference = new Preference();
                preference.updateMember(member);
                preference.updateFood(food);
                preferenceRepository.save(preference); // Preference 테이블에 저장

            }
        }
    }

    /**
     *
     * @param memberId
     * @param authentication
     * @return 이미 좋아요 한 사람이면 좋아요 취소/ 아니면 좋아요 누름
     */
    @Transactional
    public Long toggleLikeForPost(Long memberId, Authentication authentication) {
        Member liked = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND)); // 좋아요 받은 사람

        Member liker = memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND)); // 좋아요 누른 사람

        Optional<Likes> optionalLikes = likesRepository.findByLikedAndLiker(liked, liker);

        if(optionalLikes.isPresent()){
            likesRepository.deleteById(optionalLikes.get().getId());
            liked.setLikes(liked.getLikes() - 1);
        } else {
            likesRepository.save(Likes.makeLikes(liked, liker));
            liked.setLikes(liked.getLikes() + 1);
        }
        memberRepository.save(liked);

        return liked.getLikes();
    }

        public JwtTokenDto login(MemberDto.loginRequest request) {
            Member member = memberRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));
            if (!BCrypt.checkpw(request.getPassword(), member.getPassword())) {
                throw new MemberException(Error.LOGIN_FAILED);
            }
            if (!member.getIsEmailAuth()) {
                throw new MemberException(Error.EMAIL_AUTH_FAILED);
            }

            if (member.getIsDeleted() != null) {
                throw new MemberException(Error.DELETED_USER);
            }

            String refreshToken = jwtTokenProvider.createRefreshToken();

            jwtTokenProvider.updateRefreshToken(member.getEmail(), refreshToken);

            return JwtTokenDto.createJwtToken(jwtTokenProvider.createAccessToken(member.getId()), refreshToken);
    }


    /**
     *
     * @param request 사용자가 입력한 정보
     * @param authentication 로그인한 사용자의 정보
     * @return
     */
    @Transactional
    public void changePassword(MemberDto.passwordUpdateRequest request, Authentication authentication) {
        Member member = memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));

        if (!BCrypt.checkpw(request.getOldPassword(), member.getPassword())) {
            throw new MemberException(Error.PASSWORD_NOT_MATCH);
        }
        member.updatePassword(BCrypt.hashpw(request.getNewPassword(),BCrypt.gensalt()));
        memberRepository.save(member);
    }


    /**
     * 회원 탈퇴 (카카오 로그인 사용자)
     * @param request
     * @param response
     * @param authentication
     * @return 회원 탈퇴 성공 여부
     * 사용자가 지정한 비밀번호와 일치 하면 탈퇴 승인
     */
    @Transactional
    public void deleteKakaoMember(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Member member = memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));

        if (member.getMemberLoginType() != MemberLoginType.KAKAO) {
            throw new MemberException(Error.USER_NOT_KAKAO);
        }
        logout(request, response);
        member.setIsDeleted(LocalDateTime.now());
        memberRepository.save(member);
    }

    /**
     * 회원 탈퇴 (일반 사용자)
     * @param request
     * @param response
     * @param authentication
     * @param deleteMemberRequest
     * @return 회원 탈퇴 성공 여부
     * 사용자가 지정한 비밀번호와 일치 하면 탈퇴 승인
     */
    @Transactional
    public void deleteGeneralMember(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication,
            MemberDto.deleteMemberRequest deleteMemberRequest) {
        Member member = memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));

        if (member.getMemberLoginType() != MemberLoginType.GENERAL) {
            throw new MemberException(Error.USER_NOT_GENERAL);
        }

        if (!BCrypt.checkpw(deleteMemberRequest.getPassword(), member.getPassword())) {
            throw new MemberException(Error.PASSWORD_NOT_MATCH);
        }

        member.setIsDeleted(LocalDateTime.now());
        memberRepository.save(member);
        logout(request, response);
    }

    /**
     * @param request 사용자가 선택한 음식 종류
     * @param authentication 사용자의 정보
     * @return
     */
    @Transactional
    public void changePreferenceFood(MemberDto.changePreferenceFoodRequest request, Authentication authentication) {
        Member member = memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));

        preferenceRepository.deleteByMember(member);
        processFoodPreferences(member, request.getFood());
    }

    @Transactional
    public void resetPassword(MemberDto.emailRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));

        String resetPassword = RandomStringMaker.randomStringMaker();
        sendMailResetPassword(request.getEmail(), resetPassword);

        member.setPassword(BCrypt.hashpw(resetPassword, BCrypt.gensalt()));
        memberRepository.save(member);
    }
}
