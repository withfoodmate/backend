package com.foodmate.backend.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.foodmate.backend.dto.GroupDto;
import com.foodmate.backend.entity.ChatRoom;
import com.foodmate.backend.entity.Food;
import com.foodmate.backend.entity.FoodGroup;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.EnrollmentStatus;
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
  public static final Long memberId2 = 2L;
  public static final Long foodId = 1L;
  public static final Long groupId = 1L;

  private static final String TITLE = "치킨 먹을 사람~";
  private static final String NAME = "치킨 모임";
  private static final String CONTENT = "치킨 먹을 사람 구해요!";
  private static final String TYPE = "치킨";
  private static final LocalDate VALID_DATE = LocalDate.parse("2023-11-04");
  private static final LocalTime VALID_TIME = LocalTime.parse("18:30");
  private static final int MAX_PARTICIPANTS = 8;
  private static final String STORE_NAME = "BBQ 홍대점";
  private static final String STORE_ADDRESS = "서울특별시 마포구 동교동 147-4";
  private static final String LATITUDE = "33.12112";
  private static final String LONGITUDE = "127.12112";

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
        .title(TITLE)
        .name(NAME)
        .content(CONTENT)
        .food(TYPE)
        .date(VALID_DATE)
        .time(VALID_TIME)
        .maximum(MAX_PARTICIPANTS)
        .storeName(STORE_NAME)
        .storeAddress(STORE_ADDRESS)
        .latitude(LATITUDE)
        .longitude(LONGITUDE)
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
                .title(TITLE)
                .name(NAME)
                .content(CONTENT)
                .food(TYPE)
                .date(LocalDate.parse("2022-10-04"))
                .time(VALID_TIME)
                .maximum(MAX_PARTICIPANTS)
                .storeName(STORE_NAME)
                .storeAddress(STORE_ADDRESS)
                .latitude(LATITUDE)
                .longitude(LONGITUDE)
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

  @Test
  @DisplayName("특정 모임 수정 성공")
  void success_updateGroup() {

    //given
    Authentication mockAuthentication = createAuthentication();
    Member mockMember = createMockMember(memberId1);
    Food mockFood = createMockFood(foodId);
    FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember, mockFood, 1);

    given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
    given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));
    given(foodRepository.findByType("치킨")).willReturn(Optional.of(mockFood));

    //when
    groupService.updateGroup(groupId, mockAuthentication, GroupDto.Request.builder()
        .title(TITLE)
        .name(NAME)
        .content(CONTENT)
        .food(TYPE)
        .date(VALID_DATE)
        .time(VALID_TIME)
        .maximum(MAX_PARTICIPANTS)
        .storeName("자담치킨 서울홍대점")
        .storeAddress("서울 마포구 와우산로 140 1층")
        .latitude("37.5537505")
        .longitude("126.929225")
        .build()
    );

    //then
    verify(foodGroupRepository, times(1)).save(any());

  }

  @Test
  @DisplayName("특정 모임 수정 실패 - 해당 모임 생성자만 수정 가능")
  void fail_updateGroup_no_modify_permission_group() {

    //given
    Authentication mockAuthentication = createAuthentication();
    Member mockMember1 = createMockMember(memberId1);
    Member mockMember2 = createMockMember(memberId2);
    Food mockFood = createMockFood(foodId);
    FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember1, mockFood, 1);

    given(foodGroupRepository.findById(anyLong())).willReturn(Optional.of(mockGroup));
    given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember2));

    //when
    GroupException exception = assertThrows(GroupException.class,
        () -> groupService.updateGroup(groupId, mockAuthentication, GroupDto.Request.builder()
            .title(TITLE)
            .name(NAME)
            .content(CONTENT)
            .food(TYPE)
            .date(VALID_DATE)
            .time(VALID_TIME)
            .maximum(MAX_PARTICIPANTS)
            .storeName("자담치킨 서울홍대점")
            .storeAddress("서울 마포구 와우산로 140 1층")
            .latitude("37.5537505")
            .longitude("126.929225")
            .build()
        )
    );

    //then
    assertEquals(Error.NO_MODIFY_PERMISSION_GROUP, exception.getError());

  }

  @Test
  @DisplayName("특정 모임 수정 실패 - 현재시간으로부터 한시간 이후 ~ 한달 이내의 모임만 생성 가능")
  void fail_updateGroup_out_of_date_range() {

    //given
    Authentication mockAuthentication = createAuthentication();
    Member mockMember = createMockMember(memberId1);
    Food mockFood = createMockFood(foodId);
    FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember, mockFood, 1);

    given(foodGroupRepository.findById(anyLong())).willReturn(Optional.of(mockGroup));
    given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));
    given(foodRepository.findByType("치킨")).willReturn(Optional.of(mockFood));

    //when
    GroupException exception = assertThrows(GroupException.class,
        () -> groupService.updateGroup(groupId, mockAuthentication,
            GroupDto.Request.builder()
                .title(TITLE)
                .name(NAME)
                .content(CONTENT)
                .food(TYPE)
                .date(LocalDate.parse("2022-10-04"))
                .time(VALID_TIME)
                .maximum(MAX_PARTICIPANTS)
                .storeName("자담치킨 서울홍대점")
                .storeAddress("서울 마포구 와우산로 140 1층")
                .latitude("37.5537505")
                .longitude("126.929225")
                .build()
        )
    );

    //then
    assertEquals(Error.OUT_OF_DATE_RANGE, exception.getError());

  }

  @Test
  @DisplayName("특정 모임 삭제 성공")
  void success_deleteGroup() {

    //given
    Authentication mockAuthentication = createAuthentication();
    Member mockMember = createMockMember(memberId1);
    Food mockFood = createMockFood(foodId);
    FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember, mockFood, 1);

    given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
    given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));

    //when
    groupService.deleteGroup(groupId, mockAuthentication);

    //then
    verify(foodGroupRepository, times(1)).save(mockGroup);
    verify(enrollmentRepository, times(1))
        .changeStatusByGroupId(groupId, EnrollmentStatus.GROUP_CANCEL);
    verify(chatRoomRepository, times(1)).deleteByFoodGroupId(groupId);

  }

  @Test
  @DisplayName("특정 모임 삭제 실패 - 해당 모임 생성자만 수정 가능")
  void fail_deleteGroup_no_modify_permission_group() {

    //given
    Authentication mockAuthentication = createAuthentication();
    Member mockMember1 = createMockMember(memberId1);
    Member mockMember2 = createMockMember(memberId2);
    Food mockFood = createMockFood(foodId);
    FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember1, mockFood, 1);

    given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
    given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember2));

    //when
    GroupException exception = assertThrows(GroupException.class,
        () -> groupService.deleteGroup(groupId, mockAuthentication)
    );

    //then
    assertEquals(Error.NO_DELETE_PERMISSION_GROUP, exception.getError());

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
        .title(TITLE)
        .name(NAME)
        .content(CONTENT)
        .food(mockFood)
        .groupDateTime(VALID_DATE.atTime(VALID_TIME))
        .maximum(MAX_PARTICIPANTS)
        .attendance(attendance)
        .storeName(STORE_NAME)
        .storeAddress(STORE_ADDRESS)
        .location(location)
        .build();
  }

}
