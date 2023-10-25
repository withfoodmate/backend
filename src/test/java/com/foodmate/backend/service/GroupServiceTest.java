package com.foodmate.backend.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.foodmate.backend.dto.CommentDto;
import com.foodmate.backend.dto.GroupDto;
import com.foodmate.backend.dto.NearbyGroupDto;
import com.foodmate.backend.dto.ReplyDto;
import com.foodmate.backend.dto.SearchedGroupDto;
import com.foodmate.backend.entity.ChatRoom;
import com.foodmate.backend.entity.Comment;
import com.foodmate.backend.entity.Food;
import com.foodmate.backend.entity.FoodGroup;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.entity.Reply;
import com.foodmate.backend.enums.EnrollmentStatus;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.CommentException;
import com.foodmate.backend.exception.EnrollmentException;
import com.foodmate.backend.exception.FoodException;
import com.foodmate.backend.exception.GroupException;
import com.foodmate.backend.exception.ReplyException;
import com.foodmate.backend.repository.ChatRoomRepository;
import com.foodmate.backend.repository.CommentRepository;
import com.foodmate.backend.repository.EnrollmentRepository;
import com.foodmate.backend.repository.FoodGroupRepository;
import com.foodmate.backend.repository.FoodRepository;
import com.foodmate.backend.repository.MemberRepository;
import com.foodmate.backend.repository.ReplyRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public static final Long commentId = 1L;
    public static final Long replyId = 1L;

    public static final int pageNumber = 0;
    public static final int pageSize = 20;

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
    private static final String COMMENT = "댓글 내용";
    private static final String REPLY = "대댓글 내용";
    private static final String UPDATE_COMMENT = "수정된 댓글 내용";
    private static final String UPDATE_REPLY = "수정된 대댓글 내용";

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
                .updateStatusByGroupId(groupId, EnrollmentStatus.GROUP_CANCEL);

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

    @Test
    @DisplayName("특정 모임 신청 성공")
    void success_enrollInGroup() {

        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember1 = createMockMember(memberId1);
        Member mockMember2 = createMockMember(memberId2);
        Food mockFood = createMockFood(foodId);
        FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember1, mockFood, 1);

        given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember2));

        //when
        groupService.enrollInGroup(groupId, mockAuthentication);

        //then
        verify(enrollmentRepository, times(1)).save(any());

    }

    @Test
    @DisplayName("특정 모임 신청 실패 - 본인이 생성한 모임일 경우")
    void fail_enrollInGroup_cannot_apply_to_own_group() {

        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);
        Food mockFood = createMockFood(foodId);
        FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember, mockFood, 1);

        given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));

        //when
        EnrollmentException exception = assertThrows(EnrollmentException.class,
                () -> groupService.enrollInGroup(groupId, mockAuthentication)
        );

        //then
        assertEquals(Error.CANNOT_APPLY_TO_OWN_GROUP, exception.getError());

    }

    @Test
    @DisplayName("특정 모임 신청 실패 - 이미 신청 이력이 존재하는 경우")
    void fail_enrollInGroup_enrollment_history_exists() {

        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember1 = createMockMember(memberId1);
        Member mockMember2 = createMockMember(memberId2);
        Food mockFood = createMockFood(foodId);
        FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember1, mockFood, 1);

        given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember2));
        given(enrollmentRepository.existsByMemberAndFoodGroup(mockMember2,mockGroup)).willReturn(true);

        //when
        EnrollmentException exception = assertThrows(EnrollmentException.class,
                () -> groupService.enrollInGroup(groupId, mockAuthentication)
        );

        //then
        assertEquals(Error.ENROLLMENT_HISTORY_EXISTS, exception.getError());

    }

    @Test
    @DisplayName("특정 모임 신청 실패 - 해당 모임 정원이 다 찬 경우")
    void fail_enrollInGroup_group_full() {

        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember1 = createMockMember(memberId1);
        Member mockMember2 = createMockMember(memberId2);
        Food mockFood = createMockFood(foodId);
        FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember1, mockFood, 8);

        given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember2));

        //when
        EnrollmentException exception = assertThrows(EnrollmentException.class,
                () -> groupService.enrollInGroup(groupId, mockAuthentication)
        );

        //then
        assertEquals(Error.GROUP_FULL, exception.getError());

    }

    @Test
    @DisplayName("댓글 작성 성공")
    void success_addComment() {

        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);
        Food mockFood = createMockFood(foodId);
        FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember, mockFood, 1);

        given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));

        //when
        groupService.addComment(groupId, mockAuthentication, CommentDto.Request.builder()
                .content(COMMENT)
                .build()
        );

        //then
        verify(commentRepository, times(1)).save(any());

    }

    @Test
    @DisplayName("대댓글 작성 성공")
    void success_addReply() {

        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);
        Food mockFood = createMockFood(foodId);
        FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember, mockFood, 1);
        Comment mockComment = createMockComment(commentId, mockGroup, mockMember);

        given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(mockComment));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));

        //when
        groupService.addReply(groupId, commentId, mockAuthentication,
                ReplyDto.Request.builder()
                        .content(REPLY)
                        .build()
        );

        //then
        verify(replyRepository, times(1)).save(any());

    }

    @Test
    @DisplayName("댓글 수정 성공")
    void success_updateComment() {

        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);
        Food mockFood = createMockFood(foodId);
        FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember, mockFood, 1);
        Comment mockComment = createMockComment(commentId, mockGroup, mockMember);

        given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(mockComment));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));

        //when
        groupService.updateComment(groupId, commentId, mockAuthentication,
                CommentDto.Request.builder()
                        .content(UPDATE_COMMENT)
                        .build()
        );

        //then
        verify(commentRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 해당 댓글 작성자만 수정 가능")
    void fail_updateComment_no_modify_permission_comment() {

        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember1 = createMockMember(memberId1);
        Member mockMember2 = createMockMember(memberId2);
        Food mockFood = createMockFood(foodId);
        FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember1, mockFood, 1);
        Comment mockComment = createMockComment(commentId, mockGroup, mockMember1);

        given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(mockComment));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember2));

        //when
        CommentException exception = assertThrows(CommentException.class,
                () -> groupService.updateComment(groupId, commentId, mockAuthentication,
                        CommentDto.Request.builder()
                                .content(UPDATE_COMMENT)
                                .build()
                )
        );

        //then
        assertEquals(Error.NO_MODIFY_PERMISSION_COMMENT, exception.getError());

    }

    @Test
    @DisplayName("대댓글 수정 성공")
    void success_updateReply() {

        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);
        Food mockFood = createMockFood(foodId);
        FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember, mockFood, 1);
        Comment mockComment = createMockComment(commentId, mockGroup, mockMember);
        Reply mockReply = createMockReply(replyId, mockComment, mockMember);

        given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(mockComment));
        given(replyRepository.findById(replyId)).willReturn(Optional.of(mockReply));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));

        //when
        groupService.updateReply(groupId, commentId, replyId, mockAuthentication,
                ReplyDto.Request.builder()
                        .content(UPDATE_REPLY)
                        .build()
        );

        //then
        verify(replyRepository, times(1)).save(any());

    }

    @Test
    @DisplayName("대댓글 수정 실패 - 해당 대댓글 작성자만 수정 가능")
    void fail_updateReply_no_modify_permission_reply() {

        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember1 = createMockMember(memberId1);
        Member mockMember2 = createMockMember(memberId2);
        Food mockFood = createMockFood(foodId);
        FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember1, mockFood, 1);
        Comment mockComment = createMockComment(commentId, mockGroup, mockMember1);
        Reply mockReply = createMockReply(replyId, mockComment, mockMember1);

        given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(mockComment));
        given(replyRepository.findById(replyId)).willReturn(Optional.of(mockReply));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember2));

        //when
        ReplyException exception = assertThrows(ReplyException.class,
                () -> groupService.updateReply(groupId, commentId, replyId, mockAuthentication,
                        ReplyDto.Request.builder()
                                .content(UPDATE_REPLY)
                                .build()
                )
        );

        //then
        assertEquals(Error.NO_MODIFY_PERMISSION_REPLY, exception.getError());

    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void success_deleteComment() {

        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);
        Food mockFood = createMockFood(foodId);
        FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember, mockFood, 1);
        Comment mockComment = createMockComment(commentId, mockGroup, mockMember);

        given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(mockComment));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));

        //when
        groupService.deleteComment(groupId, commentId, mockAuthentication);

        //then
        verify(replyRepository, times(1)).deleteAllByComment(mockComment);
        verify(commentRepository, times(1)).deleteById(commentId);

    }

    @Test
    @DisplayName("댓글 삭제 실패 - 해당 댓글 작성자만 삭제 가능")
    void fail_deleteComment_no_delete_permission_comment() {

        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember1 = createMockMember(memberId1);
        Member mockMember2 = createMockMember(memberId2);
        Food mockFood = createMockFood(foodId);
        FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember1, mockFood, 1);
        Comment mockComment = createMockComment(commentId, mockGroup, mockMember1);

        given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(mockComment));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember2));

        //when
        CommentException exception = assertThrows(CommentException.class,
                () -> groupService.deleteComment(groupId, commentId, mockAuthentication)
        );

        //then
        assertEquals(Error.NO_DELETE_PERMISSION_COMMENT, exception.getError());

    }

    @Test
    @DisplayName("대댓글 삭제 성공")
    void success_deleteReply() {

        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);
        Food mockFood = createMockFood(foodId);
        FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember, mockFood, 1);
        Comment mockComment = createMockComment(commentId, mockGroup, mockMember);
        Reply mockReply = createMockReply(replyId, mockComment, mockMember);

        given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(mockComment));
        given(replyRepository.findById(replyId)).willReturn(Optional.of(mockReply));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));

        //when
        groupService.deleteReply(groupId, commentId, replyId, mockAuthentication);

        //then
        verify(replyRepository, times(1)).deleteById(replyId);

    }

    @Test
    @DisplayName("대댓글 삭제 실패 - 해당 대댓글 작성자만 삭제 가능")
    void fail_deleteReply_no_delete_permission_reply() {

        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember1 = createMockMember(memberId1);
        Member mockMember2 = createMockMember(memberId2);
        Food mockFood = createMockFood(foodId);
        FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember1, mockFood, 1);
        Comment mockComment = createMockComment(commentId, mockGroup, mockMember1);
        Reply mockReply = createMockReply(replyId, mockComment, mockMember1);

        given(foodGroupRepository.findById(groupId)).willReturn(Optional.of(mockGroup));
        given(commentRepository.findById(commentId)).willReturn(Optional.of(mockComment));
        given(replyRepository.findById(replyId)).willReturn(Optional.of(mockReply));
        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember2));

        //when
        ReplyException exception = assertThrows(ReplyException.class,
                () -> groupService.deleteReply(groupId, commentId, replyId, mockAuthentication)
        );

        //then
        assertEquals(Error.NO_DELETE_PERMISSION_REPLY, exception.getError());

    }

    @Test
    @DisplayName("댓글 대댓글 전체 조회 성공")
    void success_getComments() {

        //given
        Member mockMember = createMockMember(memberId1);
        Food mockFood = createMockFood(foodId);
        FoodGroup mockGroup = createMockFoodGroup(groupId, mockMember, mockFood, 1);
        Comment mockComment = createMockComment(commentId, mockGroup, mockMember);
        Reply mockReply = createMockReply(replyId, mockComment, mockMember);

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<Comment> comments = new ArrayList<>();
        comments.add(mockComment);
        Page<Comment> commentsPage = new PageImpl<>(comments, pageable, comments.size());

        List<Reply> replies = new ArrayList<>();
        replies.add(mockReply);

        given(foodGroupRepository.findById(any())).willReturn(Optional.of(mockGroup));
        given(commentRepository.findAllByFoodGroup(any(), any())).willReturn(commentsPage);
        given(replyRepository.findAllByComment(any())).willReturn(replies);

        //when
        Page<CommentDto.Response> response = groupService.getComments(groupId, pageable);

        //then
        Comment commentContent = commentsPage.getContent().get(0);
        Reply replyContent = replies.get(0);
        CommentDto.Response responseCommentContent = response.getContent().get(0);
        ReplyDto.Response responseReplyContent = response.getContent().get(0).getReplies().get(0);

        assertAll(
                () -> assertEquals(commentContent.getId(),
                        responseCommentContent.getCommentId()),
                () -> assertEquals(commentContent.getMember().getId(),
                        responseCommentContent.getMemberId()),
                () -> assertEquals(commentContent.getMember().getNickname(),
                        responseCommentContent.getNickname()),
                () -> assertEquals(commentContent.getMember().getImage(),
                        responseCommentContent.getImage()),
                () -> assertEquals(commentContent.getContent(),
                        responseCommentContent.getContent()),
                () -> assertEquals(commentContent.getCreatedDate(),
                        responseCommentContent.getCreatedDate()),
                () -> assertEquals(commentContent.getUpdatedDate(),
                        responseCommentContent.getUpdatedDate()),
                () -> assertEquals(replyContent.getId(),
                        responseReplyContent.getReplyId()),
                () -> assertEquals(replyContent.getMember().getId(),
                        responseReplyContent.getMemberId()),
                () -> assertEquals(replyContent.getMember().getNickname(),
                        responseReplyContent.getNickname()),
                () -> assertEquals(replyContent.getMember().getImage(),
                        responseReplyContent.getImage()),
                () -> assertEquals(replyContent.getContent(),
                        responseReplyContent.getContent()),
                () -> assertEquals(replyContent.getCreatedDate(),
                        responseReplyContent.getCreatedDate()),
                () -> assertEquals(replyContent.getUpdatedDate(),
                        responseReplyContent.getUpdatedDate())
        );

    }

    @Test
    @DisplayName("검색 기능 성공")
    void success_searchByKeyword() {

        //given
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<SearchedGroupDto> entities = new ArrayList<>();
        Page<SearchedGroupDto> page = new PageImpl<>(entities, pageable, entities.size());

        given(foodGroupRepository.searchByKeyword(any(), any(), any(), any())).willReturn(page);

        //when
        Page<SearchedGroupDto> response = groupService.searchByKeyword(TITLE, pageable);

        //then
        verify(foodGroupRepository, times(1))
                .searchByKeyword(any(), any(), any(), any());

        assertNotNull(response);
        assertEquals(entities.size(), response.getContent().size());

    }

    @Test
    @DisplayName("오늘 모임 조회 성공")
    void success_getTodayGroupList() {

        //given
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<SearchedGroupDto> entities = new ArrayList<>();
        Page<SearchedGroupDto> page = new PageImpl<>(entities, pageable, entities.size());

        given(foodGroupRepository.searchByDate(any(), any(), any())).willReturn(page);

        //when
        Page<SearchedGroupDto> response = groupService.getTodayGroupList(pageable);

        //then
        verify(foodGroupRepository, times(1)).searchByDate(any(), any(), any());

        assertNotNull(response);
        assertEquals(entities.size(), response.getContent().size());

    }

    @Test
    @DisplayName("전체 모임 조회 성공")
    void success_getAllGroupList() {

        //given
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<SearchedGroupDto> entities = new ArrayList<>();
        Page<SearchedGroupDto> page = new PageImpl<>(entities, pageable, entities.size());

        given(foodGroupRepository.getAllGroupList(any(), any(), any())).willReturn(page);

        //when
        Page<SearchedGroupDto> response = groupService.getAllGroupList(pageable);

        //then
        verify(foodGroupRepository, times(1)).getAllGroupList(any(), any(), any());

        assertNotNull(response);
        assertEquals(entities.size(), response.getContent().size());

    }

    @Test
    @DisplayName("거리순 조회 성공")
    void success_searchByLocation() {

        //given
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<SearchedGroupDto> entities = new ArrayList<>();
        Page<SearchedGroupDto> page = new PageImpl<>(entities, pageable, entities.size());

        given(foodGroupRepository.searchByLocation(any(), any(), any(), any())).willReturn(page);

        //when
        Page<SearchedGroupDto> response = groupService.searchByLocation(LATITUDE, LONGITUDE, pageable);

        //then
        verify(foodGroupRepository, times(1))
                .searchByLocation(any(), any(), any(), any());

        assertNotNull(response);
        assertEquals(entities.size(), response.getContent().size());

    }

    @Test
    @DisplayName("날짜별 조회 성공")
    void success_searchByDate() {

        //given
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<SearchedGroupDto> entities = new ArrayList<>();
        Page<SearchedGroupDto> page = new PageImpl<>(entities, pageable, entities.size());

        given(foodGroupRepository.searchByDate(any(), any(), any())).willReturn(page);

        //when
        Page<SearchedGroupDto> response = groupService.searchByDate(
                LocalDate.parse("2023-11-01"), LocalDate.parse("2023-11-05"), pageable);

        //then
        verify(foodGroupRepository, times(1)).searchByDate(any(), any(), any());

        assertNotNull(response);
        assertEquals(entities.size(), response.getContent().size());

    }

    @Test
    @DisplayName("메뉴별 조회 성공")
    void success_searchByFood() {

        //given
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<SearchedGroupDto> entities = new ArrayList<>();
        Page<SearchedGroupDto> page = new PageImpl<>(entities, pageable, entities.size());
        List<String> foods = new ArrayList<>();

        given(foodGroupRepository.searchByFood(any(), any(), any(), any())).willReturn(page);

        //when
        Page<SearchedGroupDto> response = groupService.searchByFood(foods, pageable);

        //then
        verify(foodGroupRepository, times(1))
                .searchByFood(any(), any(), any(), any());

        assertNotNull(response);
        assertEquals(entities.size(), response.getContent().size());

    }

    @Test
    @DisplayName("메뉴별 조회 실패 - DB에 존재하지 않는 음식")
    void fail_food_not_found() {

        //given
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<String> foods = new ArrayList<>();
        foods.add("치킨피자");

        given(foodRepository.existsByType("치킨피자")).willReturn(false);

        //when
        FoodException exception = assertThrows(FoodException.class,
                () -> groupService.searchByFood(foods, pageable)
        );

        //then
        assertEquals(Error.FOOD_NOT_FOUND, exception.getError());

    }

    @Test
    @DisplayName("내 근처 모임 성공")
    void success_getNearbyGroupList() {

        //given
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<NearbyGroupDto> entities = new ArrayList<>();
        Page<NearbyGroupDto> page = new PageImpl<>(entities, pageable, entities.size());

        given(foodGroupRepository.getNearbyGroupList(any(), any(), any(), any())).willReturn(page);

        //when
        Page<NearbyGroupDto> response = groupService.getNearbyGroupList(LATITUDE, LONGITUDE, pageable);

        //then
        verify(foodGroupRepository, times(1))
                .getNearbyGroupList(any(), any(), any(), any());

        assertNotNull(response);
        assertEquals(entities.size(), response.getContent().size());

    }

    @Test
    @DisplayName("로그인한 사용자가 참여한 모임 조회 성공")
    void success_getAcceptedGroupList() {

        //given
        Authentication mockAuthentication = createAuthentication();
        Member mockMember = createMockMember(memberId1);

        given(memberRepository.findByEmail(mockAuthentication.getName())).willReturn(Optional.of(mockMember));

        //when
        groupService.getAcceptedGroupList(mockAuthentication);

        //then
        verify(enrollmentRepository, times(1))
                .getAcceptedGroupList(any(), any(), any(), any());

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

    private Comment createMockComment(Long commentId, FoodGroup mockGroup, Member mockMember) {
        return Comment.builder()
                .id(commentId)
                .foodGroup(mockGroup)
                .member(mockMember)
                .content(COMMENT)
                .createdDate(VALID_DATE.atTime(VALID_TIME))
                .build();
    }

    private Reply createMockReply(Long replyId, Comment mockComment, Member mockMember) {
        return Reply.builder()
                .id(replyId)
                .comment(mockComment)
                .member(mockMember)
                .content(REPLY)
                .createdDate(VALID_DATE.atTime(VALID_TIME))
                .build();
    }
}