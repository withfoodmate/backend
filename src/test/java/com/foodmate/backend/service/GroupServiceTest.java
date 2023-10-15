package com.foodmate.backend.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.foodmate.backend.dto.GroupDto;
import com.foodmate.backend.entity.ChatRoom;
import com.foodmate.backend.entity.Food;
import com.foodmate.backend.entity.FoodGroup;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.GroupException;
import com.foodmate.backend.repository.ChatRoomRepository;
import com.foodmate.backend.repository.CommentRepository;
import com.foodmate.backend.repository.EnrollmentRepository;
import com.foodmate.backend.repository.FoodGroupRepository;
import com.foodmate.backend.repository.FoodRepository;
import com.foodmate.backend.repository.MemberRepository;
import com.foodmate.backend.repository.ReplyRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

@ExtendWith(MockitoExtension.class)
public class GroupServiceTest {

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

  @Mock
  private CommentRepository commentRepository;

  @Mock
  private ReplyRepository replyRepository;
  @InjectMocks
  private GroupService groupService;

  public static final Long memberId1 = 1L;
  public static final Long foodId = 1L;
  public static final Long groupId = 1L;

  @Test
  @DisplayName("모임 생성 성공")
  void success_addGroup() {

    //given
    Authentication mockAuthentication = createAuthentication();
    Member mockMember = createMockMember(memberId1);
    Food mockFood = createMockFood(foodId);

    given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));
    given(foodRepository.findByType("치킨")).willReturn(Optional.of(mockFood));

    //when
    groupService.addGroup(mockAuthentication, GroupDto.Request.builder()
        .title("치킨 먹을 사람~")
        .name("치킨 모임")
        .content("치킨 먹을 사람 구해요!")
        .food("치킨")
        .date(LocalDate.parse("2023-11-04"))
        .time(LocalTime.parse("18:30"))
        .maximum(8)
        .storeName("BBQ 홍대점")
        .storeAddress("서울특별시 마포구 동교동 147-4")
        .latitude("33.12112")
        .longitude("127.12112")
        .build()
    );

    //then
    verify(foodGroupRepository, times(1)).save(any());
    verify(chatRoomRepository, times(1)).save(any());

  }

  @Test
  @DisplayName("모임 생성 실패 - 현재시간으로부터 한시간 이후 ~ 한달 이내의 모임만 생성 가능")
  void fail_addGroup_out_of_date_range() {

    //given
    Authentication mockAuthentication = createAuthentication();
    Member mockMember = createMockMember(memberId1);
    Food mockFood = createMockFood(foodId);

    given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));
    given(foodRepository.findByType("치킨")).willReturn(Optional.of(mockFood));

    //when
    GroupException exception = assertThrows(GroupException.class,
        () -> groupService.addGroup(mockAuthentication,
            GroupDto.Request.builder()
                .title("치킨 먹을 사람~")
                .name("치킨 모임")
                .content("치킨 먹을 사람 구해요!")
                .food("치킨")
                .date(LocalDate.parse("2022-10-04"))
                .time(LocalTime.parse("18:30"))
                .maximum(8)
                .storeName("BBQ 홍대점")
                .storeAddress("서울특별시 마포구 동교동 147-4")
                .latitude("33.12112")
                .longitude("127.12112")
                .build()
        )
    );

    //then
    assertEquals(Error.OUT_OF_DATE_RANGE, exception.getError());

  }

  @Test
  @DisplayName("특정 모임 상세 조회 성공")
  void success_getGroupDetail() {

    //given
    Member mockMember = createMockMember(memberId1);
    Food mockFood = createMockFood(foodId);
    FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember, mockFood, 1);

    Long chatRoomId = 1L;
    ChatRoom mockChatRoom = new ChatRoom();
    mockChatRoom.setId(chatRoomId);

    given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
    given(chatRoomRepository.findByFoodGroupId(groupId)).willReturn(Optional.of(mockChatRoom));

    //when
    GroupDto.DetailResponse response = groupService.getGroupDetail(groupId);

    //then
    assertAll(
        () -> assertEquals(mockGroup.getId(),
            response.getGroupId()),
        () -> assertEquals(mockGroup.getMember().getId(),
            response.getMemberId()),
        () -> assertEquals(mockGroup.getMember().getNickname(),
            response.getNickname()),
        () -> assertEquals(mockGroup.getMember().getImage(),
            response.getImage()),
        () -> assertEquals(mockGroup.getTitle(),
            response.getTitle()),
        () -> assertEquals(mockGroup.getName(),
            response.getName()),
        () -> assertEquals(mockGroup.getContent(),
            response.getContent()),
        () -> assertEquals(mockGroup.getFood().getType(),
            response.getFood()),
        () -> assertEquals(mockGroup.getGroupDateTime().toLocalDate(),
            response.getDate()),
        () -> assertEquals(mockGroup.getGroupDateTime().toLocalTime(),
            response.getTime()),
        () -> assertEquals(mockGroup.getMaximum(),
            response.getMaximum()),
        () -> assertEquals(mockGroup.getAttendance(),
            response.getCurrent()),
        () -> assertEquals(mockGroup.getStoreName(),
            response.getStoreName()),
        () -> assertEquals(mockGroup.getStoreAddress(),
            response.getStoreAddress()),
        () -> assertEquals(mockGroup.getLocation().getY(),
            Double.parseDouble(response.getLatitude())),
        () -> assertEquals(mockGroup.getLocation().getX(),
            Double.parseDouble(response.getLongitude())),
        () -> assertEquals(mockGroup.getCreatedDate(),
            response.getCreatedDate()),
        () -> assertEquals(chatRoomId,
            response.getChatRoomId())
    );

  }

  private Authentication createAuthentication() {

    String email = "dlaehdgus23@naver.com";
    String password = "ehdgus1234";

    Authentication mockAuthentication = new UsernamePasswordAuthenticationToken(email, password,
        AuthorityUtils.createAuthorityList("ROLE_USER"));

    return mockAuthentication;
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
        .title("치킨 먹을 사람~")
        .name("치킨 모임")
        .content("치킨 먹을 사람 구해요!")
        .food(mockFood)
        .groupDateTime(LocalDateTime.parse("2021-01-01T15:39:30"))
        .maximum(8)
        .attendance(attendance)
        .storeName("BBQ 홍대점")
        .storeAddress("서울특별시 마포구 동교동 147-4")
        .location(location)
        .build();
  }

}
