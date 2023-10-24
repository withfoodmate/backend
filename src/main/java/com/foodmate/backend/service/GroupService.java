package com.foodmate.backend.service;

import com.foodmate.backend.dto.*;
import com.foodmate.backend.entity.*;
import com.foodmate.backend.enums.EnrollmentStatus;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.*;
import com.foodmate.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private static final int RESERVATION_INTERVAL_HOUR = 1;
    private static final int RESERVATION_RANGE_MONTH = 1;
    private static final int SEARCH_INTERVAL_MINUTE = 10;

    private final MemberRepository memberRepository;
    private final FoodRepository foodRepository;
    private final FoodGroupRepository foodGroupRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;

    // 모임 생성
    public void addGroup(Authentication authentication, GroupDto.Request request) {

        Member member = getMember(authentication);

        // 입력된 음식 정보 올바른지 체크
        Food food = validateFood(request.getFood());

        // 입력된 모임일시 올바른지 체크
        LocalDateTime groupDateTime = validateDateTime(request);

        // 좌표 생성
        Point storeLocation = getPoint(request.getLatitude(), request.getLongitude());

        FoodGroup foodGroup = FoodGroup.builder()
                .member(member)
                .title(request.getTitle())
                .name(request.getName())
                .content(request.getContent())
                .food(food)
                .groupDateTime(groupDateTime)
                .maximum(request.getMaximum())
                .attendance(1)
                .storeName(request.getStoreName())
                .storeAddress(request.getStoreAddress())
                .location(storeLocation)
                .build();

        // 모임 저장
        foodGroupRepository.save(foodGroup);

        // 채팅방 생성
        chatRoomRepository.save(new ChatRoom(foodGroup));
    }

    // 특정 모임 상세 조회
    public GroupDto.DetailResponse getGroupDetail(Long groupId) {

        FoodGroup group = validateGroupId(groupId);

        ChatRoom chatRoom = chatRoomRepository.findByFoodGroupId(groupId)
                .orElseThrow(() -> new ChatException(Error.CHATROOM_NOT_FOUND));

        return GroupDto.DetailResponse.createGroupDetailResponse(group, chatRoom);
    }

    // 특정 모임 수정
    public void updateGroup(Long groupId, Authentication authentication, GroupDto.Request request) {

        FoodGroup group = validateGroupId(groupId);

        Member member = getMember(authentication);

        // 해당 모임 생성자만 수정 가능
        if (group.getMember().getId() != member.getId()) {
            throw new GroupException(Error.NO_MODIFY_PERMISSION_GROUP);
        }

        // 입력된 음식 정보 올바른지 체크
        Food food = validateFood(request.getFood());

        // 입력된 모임일시 올바른지 체크
        LocalDateTime groupDateTime = validateDateTime(request);

        // 좌표 생성
        Point storeLocation = getPoint(request.getLatitude(), request.getLongitude());

        group.setTitle(request.getTitle());
        group.setName(request.getName());
        group.setContent(request.getContent());
        group.setFood(food);
        group.setGroupDateTime(groupDateTime);
        group.setMaximum(request.getMaximum());
        group.setStoreName(request.getStoreName());
        group.setStoreAddress(request.getStoreAddress());
        group.setLocation(storeLocation);

        foodGroupRepository.save(group);
    }

    // TODO 삭제된 모임의 댓글 대댓글 일괄삭제 - 스케쥴링으로 하루에 한번?
    // 특정 모임 삭제
    @Transactional
    public void deleteGroup(Long groupId, Authentication authentication) {

        FoodGroup group = validateGroupId(groupId);

        Member member = getMember(authentication);

        // 해당 모임 생성자만 삭제 가능
        if (group.getMember().getId() != member.getId()) {
            throw new GroupException(Error.NO_DELETE_PERMISSION_GROUP);
        }

        group.setIsDeleted(LocalDateTime.now());
        foodGroupRepository.save(group);

        // 해당 모임에 신청한 모임신청목록들의 상태를 모임취소로 일괄 변경
        enrollmentRepository.updateStatusByGroupId(groupId, EnrollmentStatus.GROUP_CANCEL);
    }

    // 특정 모임 신청
    public void enrollInGroup(Long groupId, Authentication authentication) {

        FoodGroup group = validateGroupId(groupId);

        Member member = getMember(authentication);

        // 본인이 생성한 모임일 경우
        if (group.getMember().getId() == member.getId()) {
            throw new EnrollmentException(Error.CANNOT_APPLY_TO_OWN_GROUP);
        }

        // 이미 신청 이력이 존재할 경우
        if (enrollmentRepository.existsByMemberAndFoodGroup(member, group)) {
            throw new EnrollmentException(Error.ENROLLMENT_HISTORY_EXISTS);
        }

        // 모임 현재인원 체크
        if (group.getAttendance() >= group.getMaximum()) {
            throw new EnrollmentException(Error.GROUP_FULL);
        }

        enrollmentRepository.save(Enrollment.builder()
                .member(member)
                .foodGroup(group)
                .status(EnrollmentStatus.SUBMIT)
                .build());
    }

    // 댓글 작성
    public void addComment(Long groupId, Authentication authentication, CommentDto.Request request) {

        FoodGroup group = validateGroupId(groupId);

        Member member = getMember(authentication);

        commentRepository.save(Comment.builder()
                .foodGroup(group)
                .member(member)
                .content(request.getContent())
                .build());
    }

    // 대댓글 작성
    public void addReply(Long groupId, Long commentId, Authentication authentication, ReplyDto.Request request) {

        validateGroupId(groupId);
        Comment comment = validateCommentId(groupId, commentId);

        Member member = getMember(authentication);

        replyRepository.save(Reply.builder()
                .comment(comment)
                .member(member)
                .content(request.getContent())
                .build());
    }

    // 댓글 수정
    public void updateComment(Long groupId, Long commentId, Authentication authentication, CommentDto.Request request) {

        validateGroupId(groupId);
        Comment comment = validateCommentId(groupId, commentId);

        Member member = getMember(authentication);

        // 해당 댓글 작성자만 수정 가능
        if (comment.getMember().getId() != member.getId()) {
            throw new CommentException(Error.NO_MODIFY_PERMISSION_COMMENT);
        }

        comment.setContent(request.getContent());

        commentRepository.save(comment);
    }

    // 대댓글 수정
    public void updateReply(Long groupId, Long commentId, Long replyId, Authentication authentication, ReplyDto.Request request) {

        validateGroupId(groupId);
        validateCommentId(groupId, commentId);
        Reply reply = validateReplyId(commentId, replyId);

        Member member = getMember(authentication);

        // 해당 대댓글 작성자만 수정 가능
        if (reply.getMember().getId() != member.getId()) {
            throw new ReplyException(Error.NO_MODIFY_PERMISSION_REPLY);
        }

        reply.setContent(request.getContent());

        replyRepository.save(reply);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long groupId, Long commentId, Authentication authentication) {

        validateGroupId(groupId);
        Comment comment = validateCommentId(groupId, commentId);

        Member member = getMember(authentication);

        // 해당 댓글 작성자만 삭제 가능
        if (comment.getMember().getId() != member.getId()) {
            throw new CommentException(Error.NO_DELETE_PERMISSION_COMMENT);
        }

        // 우선 해당 댓글의 대댓글 일괄 삭제해야 댓글 삭제 가능
        replyRepository.deleteAllByComment(comment);

        commentRepository.deleteById(commentId);
    }

    // 대댓글 삭제
    public void deleteReply(Long groupId, Long commentId, Long replyId, Authentication authentication) {

        validateGroupId(groupId);
        validateCommentId(groupId, commentId);
        Reply reply = validateReplyId(commentId, replyId);

        Member member = getMember(authentication);

        // 해당 대댓글 작성자만 삭제 가능
        if (reply.getMember().getId() != member.getId()) {
            throw new ReplyException(Error.NO_DELETE_PERMISSION_REPLY);
        }

        replyRepository.deleteById(replyId);
    }

    // 댓글 대댓글 전체 조회
    public Page<CommentDto.Response> getComments(Long groupId, Pageable pageable) {

        FoodGroup group = validateGroupId(groupId);

        // 댓글 전체 조회
        Page<Comment> comments = commentRepository.findAllByFoodGroup(group, pageable);

        Page<CommentDto.Response> commentDTOs = comments.map(comment -> {

            List<ReplyDto.Response> replyDTOs = new ArrayList<>();

            // 해당 댓글의 대댓글 전체 조회
            List<Reply> replies = replyRepository.findAllByComment(comment);

            // 대댓글 DTO 로 변환 & 리스트에 추가
            for (Reply reply : replies) {
                replyDTOs.add(ReplyDto.Response.createReplyResponse(reply));
            }

            // 댓글 DTO 로 변환 (안에 대댓글 리스트 세팅)
            return CommentDto.Response.createCommentResponse(comment, replyDTOs);
        });

        return commentDTOs;
    }

    // 검색 기능
    public Page<SearchedGroupDto> searchByKeyword(String keyword, Pageable pageable) {

        LocalDateTime current = LocalDateTime.now();

        return foodGroupRepository.searchByKeyword(keyword,
                current.plusMinutes(SEARCH_INTERVAL_MINUTE),
                current.plusMonths(RESERVATION_RANGE_MONTH), pageable);
    }

    // 오늘 모임 조회
    public Page<SearchedGroupDto> getTodayGroupList(Pageable pageable) {

        return foodGroupRepository.searchByDate(LocalDateTime.now(),
                LocalDate.now().atTime(23, 59, 59), pageable);
    }

    // 전체 모임 조회
    public Page<SearchedGroupDto> getAllGroupList(Pageable pageable) {

        LocalDateTime current = LocalDateTime.now();

        return foodGroupRepository.getAllGroupList(
                current.plusMinutes(SEARCH_INTERVAL_MINUTE),
                current.plusMonths(RESERVATION_RANGE_MONTH), pageable);
    }

    // 거리순 조회
    public Page<SearchedGroupDto> searchByLocation(String latitude, String longitude, Pageable pageable) {

        Point userLocation = getPoint(latitude, longitude);

        LocalDateTime current = LocalDateTime.now();

        return foodGroupRepository.searchByLocation(userLocation,
                current.plusMinutes(SEARCH_INTERVAL_MINUTE),
                current.plusMonths(RESERVATION_RANGE_MONTH), pageable);
    }

    // 날짜별 조회
    public Page<SearchedGroupDto> searchByDate(LocalDate start, LocalDate end, Pageable pageable) {

        LocalDateTime searchStart = (start.isEqual(LocalDate.now())) ?
                LocalDateTime.now().plusMinutes(SEARCH_INTERVAL_MINUTE) : start.atStartOfDay();
        LocalDateTime searchEnd = end.atTime(LocalTime.MAX);

        return foodGroupRepository.searchByDate(searchStart, searchEnd, pageable);
    }

    // 메뉴별 조회
    public Page<SearchedGroupDto> searchByFood(List<String> foods, Pageable pageable) {

        // foods 검증
        for (String foodType : foods) {
            if (!foodRepository.existsByType(foodType)) {
                throw new FoodException(Error.FOOD_NOT_FOUND);
            }
        }

        LocalDateTime current = LocalDateTime.now();

        return foodGroupRepository.searchByFood(foods,
                current.plusMinutes(SEARCH_INTERVAL_MINUTE),
                current.plusMonths(RESERVATION_RANGE_MONTH), pageable);
    }

    // 내 근처 모임
    public Page<NearbyGroupDto> getNearbyGroupList(String latitude, String longitude, Pageable pageable) {

        Point userLocation = getPoint(latitude, longitude);

        LocalDateTime current = LocalDateTime.now();

        return foodGroupRepository.getNearbyGroupList(userLocation,
                current.plusMinutes(SEARCH_INTERVAL_MINUTE),
                current.plusMonths(RESERVATION_RANGE_MONTH), pageable);
    }

    // 로그인한 사용자가 참여한 모임 조회
    public GroupDto.AcceptedGroup getAcceptedGroupList(Authentication authentication) {

        Member member = getMember(authentication);

        LocalDateTime current = LocalDateTime.now();

        return new GroupDto.AcceptedGroup(enrollmentRepository.getAcceptedGroupList(
                member,
                EnrollmentStatus.ACCEPT,
                current.plusMinutes(SEARCH_INTERVAL_MINUTE),
                current.plusMonths(RESERVATION_RANGE_MONTH)));
    }

    // {groupId} 경로 검증 - 존재하는 그룹이면서, 삭제되지 않은 경우만 반환
    private FoodGroup validateGroupId(Long groupId) {

        FoodGroup group = foodGroupRepository.findById(groupId)
                .orElseThrow(() -> new GroupException(Error.GROUP_NOT_FOUND));

        if (group.getIsDeleted() != null) {
            throw new GroupException(Error.GROUP_DELETED);
        }

        return group;
    }

    // {commentId} 경로 검증 - 존재하는 코멘트이면서, 해당 그룹의 코멘트가 맞으면 반환
    private Comment validateCommentId(Long groupId, Long commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(Error.COMMENT_NOT_FOUND));

        if (comment.getFoodGroup().getId() != groupId) {
            throw new CommentException(Error.INVALID_ADDRESS);
        }

        return comment;
    }

    // {replyId} 경로 검증 - 존재하는 리플라이면서, 해당 코멘트의 리플라이가 맞으면 반환
    private Reply validateReplyId(Long commentId, Long replyId) {

        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new ReplyException(Error.REPLY_NOT_FOUND));

        if (reply.getComment().getId() != commentId) {
            throw new ReplyException(Error.INVALID_ADDRESS);
        }

        return reply;
    }

    private Member getMember(Authentication authentication) {
        return memberRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));
    }

    private Food validateFood(String foodName) {
        return foodRepository.findByType(foodName)
                .orElseThrow(() -> new FoodException(Error.FOOD_NOT_FOUND));
    }

    private LocalDateTime validateDateTime(GroupDto.Request request) {

        // 입력된 모임일시
        LocalDateTime groupDateTime = request.getDate().atTime(request.getTime());

        LocalDateTime current = LocalDateTime.now();

        // 현재시간으로부터 한시간 이후 ~ 한달 이내의 모임만 생성 가능
        if (groupDateTime.isBefore(current.plusHours(RESERVATION_INTERVAL_HOUR)) ||
                groupDateTime.isAfter(current.plusMonths(RESERVATION_RANGE_MONTH))) {
            throw new GroupException(Error.OUT_OF_DATE_RANGE);
        }

        return groupDateTime;
    }

    private Point getPoint(String latitude, String longitude) {
        return new GeometryFactory().createPoint(
                new Coordinate(Double.parseDouble(longitude), Double.parseDouble(latitude)));
    }

}
