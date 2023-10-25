package com.foodmate.backend.service;

import com.foodmate.backend.dto.EnrollmentDto;
import com.foodmate.backend.entity.Enrollment;
import com.foodmate.backend.entity.Food;
import com.foodmate.backend.entity.FoodGroup;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.EnrollmentStatus;
import com.foodmate.backend.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private FoodGroupRepository foodGroupRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    public static final Long memberId1 = 1L;
    public static final Long memberId2 = 2L;
    public static final Long memberId3 = 3L;
    private static final String IMAGE = "/image/test/1";
    private static final String NICKNAME = "테스트용";
    public static final Long foodId = 1L;
    public static final Long groupId = 1L;
    public static final Long foodGroupId = 1L;
    public static final Long enrollmentId1 = 1L;
    public static final Long enrollmentId2 = 2L;

    private static final String TITLE = "치킨 먹을 사람~";
    private static final String NAME = "치킨 모임";

    private static final String CONTENT = "치킨 먹을 사람 구해요!";
    private static final String TYPE = "치킨";
    private static final LocalDateTime FOOD_GROUP_DATETIME = LocalDateTime.parse(("2023-11-04T10:00:00"));
    private static final LocalDateTime CURRENT_DATE_NOW = LocalDateTime.parse(("2023-11-01T10:00:00"));
    private static final int MAX_PARTICIPANTS = 8;
    private static final String STORE_NAME = "BBQ 홍대점";
    private static final String STORE_ADDRESS = "서울특별시 마포구 동교동 147-4";
    private static final String LATITUDE = "33.12112";
    private static final String LONGITUDE = "127.12112";

    @Test
    @DisplayName("본인이 생성한 모든 모임(processed)의 요청 조회 성공")
    void success_enrollmentProcessedList() {

        //given

        Authentication mockAuthentication = createAuthentication();
        String decision = "processed";
        Member mockMember1 = createMockMember(memberId1);
        Member mockMember2 = createMockMember(memberId2);
        Member mockMember3 = createMockMember(memberId3);
        Pageable pageable = PageRequest.of(0, 20);
        List<Enrollment> enrollments = new ArrayList<>();

        Food mockFood = createMockFood(foodId);
        FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember1, mockFood, 8);

        enrollments.add(createMockEnrollment(enrollmentId1, mockMember2, mockGroup));
        enrollments.add(createMockEnrollment(enrollmentId2, mockMember3, mockGroup));

        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember1));
        given(enrollmentRepository.findByMyEnrollmentProcessedListWithStatus(memberId1, EnrollmentStatus.ACCEPT, pageable))
                .willReturn(new PageImpl<>(enrollments));


        //when
        Page<EnrollmentDto.myEnrollmentReceiveResponse> result = enrollmentService.enrollmentList(decision, mockAuthentication, pageable);

        //then
        assertEquals(enrollments.size(), result.getContent().size());
        assertEquals(enrollments.size(), 2);
    }


    /**
     * test용 테이터 생성 메서드
     */

    private Authentication createAuthentication() {

        String email = "dlaehdgus23@naver.com";
        String password = "ehdgus1234";

        return new UsernamePasswordAuthenticationToken(email, password,
                AuthorityUtils.createAuthorityList("ROLE_USER"));
    }

    private Member createMockMember(Long memberId) {
        return Member.builder()
                .id(memberId)
                .email("dlaehdgus23@naver.com")
                .nickname("동현")
                .password("ehdgus1234")
                .image("123adf")
                .build();
    }

    private Food createMockFood(Long foodId) {
        return Food.builder()
                .id(foodId)
                .type("치킨")
                .image("abc123")
                .build();
    }

    private FoodGroup createMockFoodGroup(Long groupId, Member mockMember, Food mockFood, int attendance) {
        String longitude = "126.926176";
        String latitude = "37.5591095";

        Point location = new GeometryFactory().createPoint(
                new Coordinate(Double.parseDouble(longitude), Double.parseDouble(latitude)));

        return FoodGroup.builder()
                .id(groupId)
                .member(mockMember)
                .title(TITLE)
                .name(NAME)
                .content(CONTENT)
                .food(mockFood)
                .groupDateTime(FOOD_GROUP_DATETIME)
                .maximum(MAX_PARTICIPANTS)
                .attendance(attendance)
                .storeName(STORE_NAME)
                .storeAddress(STORE_ADDRESS)
                .location(location)
                .build();
    }

    private Enrollment createMockEnrollment(Long enrollmentId, Member mockMember, FoodGroup mockFoodGroup) {
        return Enrollment.builder()
                .id(enrollmentId)
                .status(EnrollmentStatus.ACCEPT)
                .foodGroup(mockFoodGroup)
                .member(mockMember)
                .build();
    }
}

