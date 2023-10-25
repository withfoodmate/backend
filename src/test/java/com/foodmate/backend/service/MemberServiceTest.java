package com.foodmate.backend.service;

import com.foodmate.backend.dto.MemberDto;
import com.foodmate.backend.entity.Food;
import com.foodmate.backend.entity.Likes;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.entity.Preference;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.enums.MemberLoginType;
import com.foodmate.backend.exception.FoodException;
import com.foodmate.backend.exception.MemberException;
import com.foodmate.backend.repository.FoodRepository;
import com.foodmate.backend.repository.LikesRepository;
import com.foodmate.backend.repository.MemberRepository;
import com.foodmate.backend.repository.PreferenceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PreferenceRepository preferenceRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private LikesRepository likesRepository;


    @InjectMocks
    private MemberService memberService;

    private MockMultipartFile mockMultipartFile = new MockMultipartFile(
            "file",
            "example.txt",
            "text/plain",
            "This is a test file".getBytes()
    );
    public static final Long memberId1 = 1L;
    public static final Long foodId1 = 1L;

    private final String s3BucketFolderName = "profile-images/";



    @Test
    @DisplayName("내 정보 가져오기 성공 - 기본 이미지/선호음식 없음")
    void success_getDefaultImageMemberInfo() {
        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockDefaultImageMember(memberId1);

        List<Preference> mockPreference = new ArrayList<>();


        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));
        given(preferenceRepository.findAllByMember(mockMember)).willReturn(mockPreference);

        //when
        MemberDto.myMemberInfoResponse response = memberService.getMemberInfo(mockAuthentication);

        //then
        assertAll(
                () -> assertEquals(mockMember.getId(), response.getMemberId()),
                () -> assertEquals(mockMember.getEmail(), response.getEmail()),
                () -> assertEquals(mockMember.getImage(), response.getImage()),
                () -> assertEquals(mockMember.getLikes(), response.getLikes()),
                () -> assertEquals(mockMember.getNickname(), response.getNickname()),
                () -> assertNotNull(response.getFood()),
                () -> assertTrue(response.getFood().isEmpty())
        );

    }

    @Test
    @DisplayName("내 정보 가져오기 성공/선호음식 없음")
    void success_getMemberInfo() {
        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);
        List<Preference> mockPreference = new ArrayList<>();


        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));
        given(preferenceRepository.findAllByMember(mockMember)).willReturn(mockPreference);
        //when
        MemberDto.myMemberInfoResponse response = memberService.getMemberInfo(mockAuthentication);

        //then
        assertAll(
                () -> assertEquals(mockMember.getId(), response.getMemberId()),
                () -> assertEquals(mockMember.getEmail(), response.getEmail()),
                () -> assertEquals(mockMember.getImage(), response.getImage()),
                () -> assertEquals(mockMember.getLikes(), response.getLikes()),
                () -> assertEquals(mockMember.getNickname(), response.getNickname()),
                () -> assertNotNull(response.getFood()),
                () -> assertTrue(response.getFood().isEmpty())
        );

    }

    @Test
    @DisplayName("내 정보 가져오기 성공 - 기본 이미지/선호음식 있음")
    void success_getMemberInfoWithPreference() {
        // given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);

        Food mockFood = createFood(1L); // 유효한 음식 ID로 생성

        List<Preference> mockPreference = new ArrayList<>();
        mockPreference.add(new Preference(1L, mockMember, mockFood)); // 선호음식 추가
        List<String> mockFoods = new ArrayList<>();
        mockFoods.add(mockFood.getType()); // 선호음식 추가


        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));
        given(preferenceRepository.findAllByMember(mockMember)).willReturn(mockPreference);
        given(foodRepository.findById(mockFood.getId())).willReturn(Optional.of(mockFood)); // 음식을 찾을 수 있도록 설정

        // when
        MemberDto.myMemberInfoResponse response = memberService.getMemberInfo(mockAuthentication);

        // then
        assertAll(
                () -> assertEquals(mockMember.getId(), response.getMemberId()),
                () -> assertEquals(mockMember.getEmail(), response.getEmail()),
                () -> assertEquals(mockMember.getImage(), response.getImage()),
                () -> assertEquals(mockMember.getLikes(), response.getLikes()),
                () -> assertEquals(mockMember.getNickname(), response.getNickname()),
                () -> assertEquals(mockFoods, response.getFood())
        );
    }


    @Test
    @DisplayName("내 정보 가져오기 실패 - 유저 없음")
    void fail_getMemberInfoNotFoundMember() {
        // given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember = null; // 사용자를 찾지 못했음을 나타내기 위해 null로 설정

        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.ofNullable(mockMember)); // Optional.ofNullable 사용

        // when
        MemberException exception = assertThrows(MemberException.class, () -> memberService.getMemberInfo(mockAuthentication));

        // then
        assertEquals(Error.USER_NOT_FOUND, exception.getError());
    }


    @Test
    @DisplayName("내 정보 가져오기 실패 - 해당 음식 없음")
    void fail_getMemberInfoNotFoundFood() {
        // given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);

        List<Preference> mockPreferences = List.of(new Preference(1L, mockMember, new Food(20L, "dasdas", "dasdsa")));

        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));
        given(preferenceRepository.findAllByMember(mockMember)).willReturn(mockPreferences);


        // when
        FoodException exception = assertThrows(FoodException.class, () -> memberService.getMemberInfo(mockAuthentication));

        // then
        assertEquals(Error.FOOD_NOT_FOUND, exception.getError());
    }


    @Test
    @DisplayName("이메일 유효성 검사/이메일 사용 가능")
    void checkDuplicateEmailUse() {
        // given
        String email = "test@tes.com";
        given(memberRepository.findByEmail(email)).willReturn(Optional.ofNullable(null));

        // when
        boolean result = memberService.checkDuplicateEmail(email);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("이메일 유효성 검사/이메일 사용 불가")
    void checkDuplicateEmailUnUse() {
        // given
        String email = "dlaehdgus23@naver.com";
        Member mockMember = createMockMember(memberId1);
        given(memberRepository.findByEmail(email)).willReturn(Optional.of(mockMember));

        // when
        boolean result = memberService.checkDuplicateEmail(email);

        // then
        assertFalse(result);
    }


    @Test
    @DisplayName("이메일 유효성 검사/닉네임 사용 가능")
    void checkDuplicateNicknameUse() {
        // given
        String nickname = "testNickname";
        given(memberRepository.findByNickname(nickname)).willReturn(Optional.ofNullable(null));

        // when
        boolean result = memberService.checkDuplicateNickname(nickname);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("이메일 유효성 검사/닉네임 사용 불가")
    void checkDuplicateNicknameUnUse() {
        // given
        String nickname = "동현";
        Member mockMember = createMockMember(memberId1);
        given(memberRepository.findByNickname(nickname)).willReturn(Optional.of(mockMember));

        // when
        boolean result = memberService.checkDuplicateNickname(nickname);
        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("닉네임으로 유저 정보 가져오기 좋아요 누름 상태")
    void success_getMemberInfoByNicknameIsLikes() {

        // given
        Member mockLoginMember = createMockMember(memberId1);
        Authentication mockAuthentication = createAuthentication();

        String nickname = "동현";
        Member mockOtherMember = createMockMember1(2L);
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockLoginMember));
        given(memberRepository.findByNickname(nickname)).willReturn(Optional.of(mockOtherMember));
        given(likesRepository.findByLikedAndLiker(mockOtherMember, mockLoginMember))
                .willReturn(Optional.of(new Likes(1L, mockOtherMember, mockLoginMember)));

        // when
        MemberDto.otherMemberInfoResponse response = memberService.getMemberInfoByNickname(nickname, mockAuthentication);

        // then
        assertAll(
                () -> assertEquals(mockOtherMember.getId(), response.getMemberId()),
                () -> assertEquals(mockOtherMember.getEmail(), response.getEmail()),
                () -> assertEquals(mockOtherMember.getImage(), response.getImage()),
                () -> assertEquals(mockOtherMember.getLikes(), response.getLikes()),
                () -> assertEquals(mockOtherMember.getNickname(), response.getNickname()),
                () -> assertTrue(response.isStatus())
        );
    }

    @Test
    @DisplayName("닉네임으로 유저 정보 가져오기 좋아요 누르지 않은 상태")
    void success_getMemberInfoByNickname() {

        // given
        Member mockLoginMember = createMockMember(memberId1);
        Authentication mockAuthentication = createAuthentication();

        String nickname = "동현";
        Member mockOtherMember = createMockMember1(2L);
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockLoginMember));
        given(memberRepository.findByNickname(nickname)).willReturn(Optional.of(mockOtherMember));
        given(likesRepository.findByLikedAndLiker(mockOtherMember, mockLoginMember))
                .willReturn(Optional.empty());

        // when
        MemberDto.otherMemberInfoResponse response = memberService.getMemberInfoByNickname(nickname, mockAuthentication);

        // then
        assertAll(
                () -> assertEquals(mockOtherMember.getId(), response.getMemberId()),
                () -> assertEquals(mockOtherMember.getEmail(), response.getEmail()),
                () -> assertEquals(mockOtherMember.getImage(), response.getImage()),
                () -> assertEquals(mockOtherMember.getLikes(), response.getLikes()),
                () -> assertEquals(mockOtherMember.getNickname(), response.getNickname()),
                () -> assertFalse(response.isStatus())
        );
    }

    @Test
    @DisplayName("닉네임으로 유저 정보 가져오기 실패 - 없는 유저")
    void fail_getMemberInfoByNicknameNotFound() {
        // given
        Member mockLoginMember = createMockMember(memberId1);
        Authentication mockAuthentication = createAuthentication();
        String nickname = "nullMember";

        given(memberRepository.findByNickname(nickname)).willReturn(Optional.empty());
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockLoginMember));
        // when

        MemberException exception = assertThrows(MemberException.class, () -> memberService.getMemberInfoByNickname(nickname, mockAuthentication));

        // then
        assertEquals(Error.USER_NOT_FOUND, exception.getError());

    }

    @Test
    @DisplayName("닉네임으로 유저 정보 가져오기 실패 - 회원 탈퇴 유저")
    void fail_getMemberInfoByNicknameDeleteUser() {
        // given
        String nickname = "deleteUser";
        Member mockLoginMember = createMockMember(memberId1);
        Authentication mockAuthentication = createAuthentication();
        Member mockOtherMember = createMockDeleteMember(2L);

        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockLoginMember));
        given(memberRepository.findByNickname(nickname)).willReturn(Optional.of(mockOtherMember));

        // when
        MemberException exception = assertThrows(MemberException.class, () -> memberService.getMemberInfoByNickname(nickname, mockAuthentication));

        // then
        assertEquals(Error.DELETED_USER, exception.getError());

    }

    @Test
    @DisplayName("다른 유저 좋아요 성공")
    void success_toggleLikeForPost() {
        // given
        Member mockLikedMember = createMockMember(1L);
        Authentication mockAuthentication = createAuthentication();
        Member mockLikerMember = createMockMember1(2L);
        Long prevLikes = mockLikedMember.getLikes();

        given(memberRepository.findById(1L)).willReturn(Optional.of(mockLikedMember));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockLikerMember));
        given(likesRepository.findByLikedAndLiker(mockLikedMember, mockLikerMember)).willReturn(Optional.empty());

        // when
        Long response =  memberService.toggleLikeForPost(1L, mockAuthentication);
        // then
        assertEquals(prevLikes + 1, response);
    }

    @Test
    @DisplayName("다른 유저 좋아요 취소 성공")
    void success_toggleCancelLikeForPost() {
        // given
        Member mockLikedMember = createMockMember(1L);
        Authentication mockAuthentication = createAuthentication();
        Member mockLikerMember = createMockMember1(2L);
        Long prevLikes = mockLikedMember.getLikes();
        Likes mockLikes = new Likes(1L,mockLikedMember, mockLikerMember);

        given(memberRepository.findById(1L)).willReturn(Optional.of(mockLikedMember));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockLikerMember));
        given(likesRepository.findByLikedAndLiker(mockLikedMember, mockLikerMember)).willReturn(Optional.of(mockLikes));

        // when
        Long response =  memberService.toggleLikeForPost(1L, mockAuthentication);
        // then
        assertEquals(prevLikes - 1, response);
    }

    @Test
    @DisplayName("다른 유저 좋아요 실패 - 로그인 유저 정보 없음")
    void fail_toggleCancelLikeForPostLoginMemberNotFound() {
        // given
        Authentication mockAuthentication = createAuthentication();
        Member mockLikedMember = createMockMember(1L);
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockLikedMember));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.empty());

        // when
        MemberException exception = assertThrows(MemberException.class, () -> memberService.toggleLikeForPost(1L, mockAuthentication));
        // then
        assertEquals(Error.USER_NOT_FOUND, exception.getError());
    }

    @Test
    @DisplayName("다른 유저 좋아요 실패 - 상대 유저 정보 없음")
    void fail_toggleCancelLikeForPostOtherMemberNotFound() {
        // given
        Authentication mockAuthentication = createAuthentication();
        given(memberRepository.findById(1L)).willReturn(Optional.empty());
        // when
        MemberException exception = assertThrows(MemberException.class, () -> memberService.toggleLikeForPost(1L, mockAuthentication));
        // then
        assertEquals(Error.USER_NOT_FOUND, exception.getError());
    }

    @Test
    @DisplayName("유저 로그아웃")
    void success_memberLogout() {
        // given
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        HttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        // then
        memberService.logoutMember(mockHttpServletRequest, mockHttpServletResponse);
    }

//    @Test
//    @DisplayName("일반유저 회원가입")
//    @Transactional
//    void success_createDefaultMember() {
//
//        // given
//        MemberDto.CreateMemberRequest request = createMemberRequest();
//        // when&then
//        memberService.createDefaultImageMember(request);
//    }  // 메일 전송 테스트 못함



    @Test
    @DisplayName("비밀번호 변경")
    void success_changePassword(){
        // given
        MemberDto.passwordUpdateRequest mockRequest =
                new MemberDto.passwordUpdateRequest("ehdgus1234", "newPassword");
        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);
        mockMember.setPassword(BCrypt.hashpw(mockMember.getPassword(), BCrypt.gensalt()));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));

        // when&then
        memberService.changePassword(mockRequest, mockAuthentication);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 해당 유저 없음")
    void fail_changePasswordUserNotFound(){
        // given
        MemberDto.passwordUpdateRequest mockRequest =
                new MemberDto.passwordUpdateRequest("ehdgus1234", "newPassword");
        Authentication mockAuthentication = createAuthentication();
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.empty());

        // when
        MemberException exception =
                assertThrows(
                        MemberException.class, () -> memberService.changePassword(mockRequest, mockAuthentication));

        // then
        assertEquals(Error.USER_NOT_FOUND, exception.getError());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 비밀번호 불일치")
    void fail_changePasswordInvalidPassword(){
        // given
        MemberDto.passwordUpdateRequest mockRequest =
                new MemberDto.passwordUpdateRequest("ehdgus123dsd4", "newPassword");
        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);
        mockMember.setPassword(BCrypt.hashpw(mockMember.getPassword(), BCrypt.gensalt()));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));


        // when
        MemberException exception =
                assertThrows(
                        MemberException.class, () -> memberService.changePassword(mockRequest, mockAuthentication));

        // then
        assertEquals(Error.PASSWORD_NOT_MATCH, exception.getError());
    }

    @Test
    @DisplayName("회원탈퇴 성공")
    void success_deleteGeneralMember(){
        // given
        MockHttpServletRequest mockHttpServletRequest =
                new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse =
                new MockHttpServletResponse();
        MemberDto.deleteMemberRequest deleteMemberRequest =
                new MemberDto.deleteMemberRequest("ehdgus1234");

        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);
        mockMember.setPassword(BCrypt.hashpw(mockMember.getPassword(), BCrypt.gensalt()));

        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));

        // when
        memberService.deleteGeneralMember(
                mockHttpServletRequest,
                mockHttpServletResponse,
                mockAuthentication,
                deleteMemberRequest
        );

    }

    @Test
    @DisplayName("회원탈퇴 실패 - 존재하는 유저 없음")
    void fail_deleteGeneralMember(){
        // given
        MockHttpServletRequest mockHttpServletRequest =
                new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse =
                new MockHttpServletResponse();
        MemberDto.deleteMemberRequest deleteMemberRequest =
                new MemberDto.deleteMemberRequest("ehdgus1234");

        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);
        mockMember.setPassword(BCrypt.hashpw(mockMember.getPassword(), BCrypt.gensalt()));

        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.empty());

        // when
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.deleteGeneralMember(
                        mockHttpServletRequest,
                        mockHttpServletResponse,
                        mockAuthentication,
                        deleteMemberRequest
                ));

        // then
        assertEquals(Error.USER_NOT_FOUND, exception.getError());
    }

    @Test
    @DisplayName("회원탈퇴 실패 - 비밀번호 틀림")
    void fail_deleteGeneralInvalidPassword(){
        // given
        MockHttpServletRequest mockHttpServletRequest =
                new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse =
                new MockHttpServletResponse();
        MemberDto.deleteMemberRequest deleteMemberRequest =
                new MemberDto.deleteMemberRequest("ehdgus123412");

        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);
        mockMember.setPassword(BCrypt.hashpw(mockMember.getPassword(), BCrypt.gensalt()));

        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));

        // when
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.deleteGeneralMember(
                        mockHttpServletRequest,
                        mockHttpServletResponse,
                        mockAuthentication,
                        deleteMemberRequest
                ));

        // then
        assertEquals(Error.PASSWORD_NOT_MATCH, exception.getError());
    }

    @Test
    @DisplayName("회원탈퇴 실패 - 일반 로그인유저 아님")
    void fail_deleteGeneralInvalidLoginType(){
        // given
        MockHttpServletRequest mockHttpServletRequest =
                new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse =
                new MockHttpServletResponse();
        MemberDto.deleteMemberRequest deleteMemberRequest =
                new MemberDto.deleteMemberRequest("ehdgus123412");

        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockKakaoMember(memberId1);


        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));

        // when
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.deleteGeneralMember(
                        mockHttpServletRequest,
                        mockHttpServletResponse,
                        mockAuthentication,
                        deleteMemberRequest
                ));

        // then
        assertEquals(Error.USER_NOT_GENERAL, exception.getError());
    }
    @Test
    @DisplayName("카카오 회원탈퇴 성공")
    void success_deleteKakaoMember(){
        // given
        MockHttpServletRequest mockHttpServletRequest =
                new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse =
                new MockHttpServletResponse();

        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockKakaoMember(memberId1);
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));

        // when
        memberService.deleteKakaoMember(
                mockHttpServletRequest,
                mockHttpServletResponse,
                mockAuthentication
        );
    }

    @Test
    @DisplayName("카카오 회원탈퇴 실패 - 유저 없음")
    void fail_deleteKakaoMember(){
        // given
        MockHttpServletRequest mockHttpServletRequest =
                new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse =
                new MockHttpServletResponse();

        Authentication mockAuthentication = createAuthentication();
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.empty());

        // when
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.deleteKakaoMember(
                        mockHttpServletRequest,
                        mockHttpServletResponse,
                        mockAuthentication
                ));

        // then
        assertEquals(Error.USER_NOT_FOUND, exception.getError());
    }

    @Test
    @DisplayName("카카오 회원탈퇴 실패 - 로그인 타입 안맞음")
    void fail_deleteKakaoMemberInvalidLoginType(){
        // given
        MockHttpServletRequest mockHttpServletRequest =
                new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse =
                new MockHttpServletResponse();

        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));

        // when
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.deleteKakaoMember(
                        mockHttpServletRequest,
                        mockHttpServletResponse,
                        mockAuthentication
                ));

        // then
        assertEquals(Error.USER_NOT_KAKAO, exception.getError());

    }

    @Test
    @DisplayName("선호음식 변경 성공")
    void success_changePreferenceFood() {
        // given
        MemberDto.changePreferenceFoodRequest changePreferenceFoodRequest =
                new MemberDto.changePreferenceFoodRequest(List.of("치킨", "피자"));

        Authentication mockAuthentication = createAuthentication();

        Member mockMember = createMockMember(memberId1);

        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));
        given(foodRepository.findByType("치킨")).willReturn(Optional.of(new Food(1L, "치킨", "gsdfg")));
        given(foodRepository.findByType("피자")).willReturn(Optional.of(new Food(2L, "피자", "test")));

        // when
        memberService.changePreferenceFood(changePreferenceFoodRequest, mockAuthentication);
    }

    @Test
    @DisplayName("선호음식 변경 성공 - 선호음식 취소")
    void success_changePreferenceFoodCancel() {
        // given
        MemberDto.changePreferenceFoodRequest changePreferenceFoodRequest =
                new MemberDto.changePreferenceFoodRequest(List.of());

        Authentication mockAuthentication = createAuthentication();

        Member mockMember = createMockMember(memberId1);

        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));

        // when
        memberService.changePreferenceFood(changePreferenceFoodRequest, mockAuthentication);
    }

    @Test
    @DisplayName("선호음식 변경 실패 - 유저 없음")
    void fail_changePreferenceFoodUserNotFound() {
        // given
        MemberDto.changePreferenceFoodRequest changePreferenceFoodRequest =
                new MemberDto.changePreferenceFoodRequest(List.of("치킨", "피자"));

        Authentication mockAuthentication = createAuthentication();

        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.empty());

        // when
        MemberException exception = assertThrows(MemberException.class,
                () -> memberService.changePreferenceFood(changePreferenceFoodRequest, mockAuthentication));

        // then
        assertEquals(Error.USER_NOT_FOUND, exception.getError());
    }

    @Test
    @DisplayName("선호음식 변경 실패 - 해당 음식 없음")
    void fail_changePreferenceFoodFoodNotFound() {
        MemberDto.changePreferenceFoodRequest changePreferenceFoodRequest =
                new MemberDto.changePreferenceFoodRequest(List.of("치킨", "피자"));

        Authentication mockAuthentication = createAuthentication();

        Member mockMember = createMockMember(memberId1);

        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));
        given(foodRepository.findByType("치킨")).willReturn(Optional.of(new Food(1L, "치킨", "gsdfg")));

        // when
        FoodException exception = assertThrows(FoodException.class,
                () -> memberService.changePreferenceFood(changePreferenceFoodRequest, mockAuthentication));

        // then
        assertEquals(Error.FOOD_NOT_FOUND, exception.getError());
    }
    private Authentication createAuthentication() {

        String email = "dlaehdgus23@naver.com";
        String password = "ehdgus1234";

        Authentication mockAuthentication = new UsernamePasswordAuthenticationToken(email, password,
                AuthorityUtils.createAuthorityList("ROLE_USER"));

        return mockAuthentication;
    }

    private Member createMockDefaultImageMember(Long memberId) {
        return Member.builder()
                .id(memberId)
                .email("dlaehdgus23@naver.com")
                .nickname("동현")
                .likes(32L)
                .image(null)
                .password("ehdgus1234")
                .build();
    }

    private Member createMockMember(Long memberId) {
        return Member.builder()
                .id(memberId)
                .email("dlaehdgus23@naver.com")
                .nickname("동현")
                .likes(32L)
                .image("asdfsadfsda")
                .password("ehdgus1234")
                .memberLoginType(MemberLoginType.GENERAL)
                .build();
    }

    private Member createMockKakaoMember(Long memberId) {
        return Member.builder()
                .id(memberId)
                .email("dlaehdgus23@naver.com")
                .nickname("동현")
                .likes(32L)
                .image("asdfsadfsda")
                .password("ehdgus1234")
                .memberLoginType(MemberLoginType.KAKAO)
                .build();
    }

    private Member createMockMember1(Long memberId) {
        return Member.builder()
                .id(memberId)
                .email("test19@naver.com")
                .nickname("test유저")
                .likes(32L)
                .image("ㅋㅊㅌㅋㅊㅌㅋ")
                .password("qasdegtr")
                .build();
    }


    private Member createMockDeleteMember(Long memberId) {
        return Member.builder()
                .id(memberId)
                .email("deleteUser@naver.com")
                .nickname("deleteUser")
                .image("asdfsadfsda")
                .password("deleteUser")
                .isDeleted(LocalDateTime.now())
                .build();
    }

    private Food createFood(Long foodId) {
        return Food.builder()
                .id(foodId)
                .image("dasdsa")
                .type("족발보쌈")
                .build();
    }

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

    private MemberDto.CreateMemberRequest createMemberRequest() {
        return MemberDto.CreateMemberRequest.builder()
                .email("test12321311@naver.con")
                .password("test124")
                .nickname("testNickName")
                .food(null)
                .build();
    }

    private MemberDto.loginRequest loginRequest() {
        return new MemberDto.loginRequest("test19@naver.com", "ehdgus1234");
    }


}