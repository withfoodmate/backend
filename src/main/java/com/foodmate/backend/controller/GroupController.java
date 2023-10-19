package com.foodmate.backend.controller;

import com.foodmate.backend.dto.*;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.GroupException;
import com.foodmate.backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    // 모임 생성
    @PostMapping
    public ResponseEntity<Void> addGroup(Authentication authentication,
                                         @RequestBody @Valid GroupDto.Request request) {
        groupService.addGroup(authentication, request);
        return ResponseEntity.ok().build();
    }

    // 특정 모임 상세 조회
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDto.DetailResponse> getGroupDetail(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupDetail(groupId));
    }

    // 특정 모임 수정
    @PutMapping("/{groupId}")
    public ResponseEntity<Void> updateGroup(@PathVariable Long groupId,
                                            Authentication authentication,
                                            @RequestBody @Valid GroupDto.Request request) {
        groupService.updateGroup(groupId, authentication, request);
        return ResponseEntity.ok().build();
    }

    // 특정 모임 삭제
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long groupId,
                                            Authentication authentication) {
        groupService.deleteGroup(groupId, authentication);
        return ResponseEntity.ok().build();
    }

    // 특정 모임 신청
    @PostMapping("/{groupId}/enrollment")
    public ResponseEntity<Void> enrollInGroup(@PathVariable Long groupId,
                                              Authentication authentication) {
        groupService.enrollInGroup(groupId, authentication);
        return ResponseEntity.ok().build();
    }

    // 댓글 작성
    @PostMapping("/{groupId}/comment")
    public ResponseEntity<Void> addComment(@PathVariable Long groupId,
                                           Authentication authentication,
                                           @RequestBody @Valid CommentDto.Request request) {
        groupService.addComment(groupId, authentication, request);
        return ResponseEntity.ok().build();
    }

    // 대댓글 작성
    @PostMapping("/{groupId}/comment/{commentId}/reply")
    public ResponseEntity<Void> addReply(@PathVariable Long groupId,
                                         @PathVariable Long commentId,
                                         Authentication authentication,
                                         @RequestBody @Valid ReplyDto.Request request) {
        groupService.addReply(groupId, commentId, authentication, request);
        return ResponseEntity.ok().build();
    }

    // 댓글 수정
    @PutMapping("/{groupId}/comment/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable Long groupId,
                                              @PathVariable Long commentId,
                                              Authentication authentication,
                                              @RequestBody @Valid CommentDto.Request request) {
        groupService.updateComment(groupId, commentId, authentication, request);
        return ResponseEntity.ok().build();
    }

    // 대댓글 수정
    @PutMapping("/{groupId}/comment/{commentId}/reply/{replyId}")
    public ResponseEntity<Void> updateReply(@PathVariable Long groupId,
                                            @PathVariable Long commentId,
                                            @PathVariable Long replyId,
                                            Authentication authentication,
                                            @RequestBody @Valid ReplyDto.Request request) {
        groupService.updateReply(groupId, commentId, replyId, authentication, request);
        return ResponseEntity.ok().build();
    }

    // 댓글 삭제
    @DeleteMapping("/{groupId}/comment/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long groupId,
                                              @PathVariable Long commentId,
                                              Authentication authentication) {
        groupService.deleteComment(groupId, commentId, authentication);
        return ResponseEntity.ok().build();
    }

    // 대댓글 삭제
    @DeleteMapping("/{groupId}/comment/{commentId}/reply/{replyId}")
    public ResponseEntity<Void> deleteReply(@PathVariable Long groupId,
                                            @PathVariable Long commentId,
                                            @PathVariable Long replyId,
                                            Authentication authentication) {
        groupService.deleteReply(groupId, commentId, replyId, authentication);
        return ResponseEntity.ok().build();
    }

    // 댓글 대댓글 전체 조회
    @GetMapping("{groupId}/comment")
    public ResponseEntity<Page<CommentDto.Response>> getComments(@PathVariable Long groupId, Pageable pageable) {
        return ResponseEntity.ok(groupService.getComments(groupId, pageable));
    }

    // 검색 기능
    @GetMapping("/search")
    public ResponseEntity<Page<SearchedGroupDto>> searchByKeyword(@RequestParam String keyword,
                                                                  Pageable pageable) {
        return ResponseEntity.ok(groupService.searchByKeyword(keyword, pageable));
    }

    // 오늘 모임 조회
    @GetMapping("/today")
    public ResponseEntity<Page<SearchedGroupDto>> getTodayGroupList(Pageable pageable) {
        return ResponseEntity.ok(groupService.getTodayGroupList(pageable));
    }

    // 전체 모임 조회
    @GetMapping("/all")
    public ResponseEntity<Page<SearchedGroupDto>> getAllGroupList(Pageable pageable) {
        return ResponseEntity.ok(groupService.getAllGroupList(pageable));
    }

    // 거리순 조회
    @GetMapping("/search/distance")
    public ResponseEntity<Page<SearchedGroupDto>> searchByLocation(@RequestParam String latitude,
                                                                   @RequestParam String longitude,
                                                                   Pageable pageable) {
        return ResponseEntity.ok(groupService.searchByLocation(latitude, longitude, pageable));
    }

    // 날짜별 조회
    @GetMapping("/search/date")
    public ResponseEntity<Page<SearchedGroupDto>> searchByDate(@RequestParam
                                                               @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
                                                               @RequestParam
                                                               @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end,
                                                               Pageable pageable) {

        if (start.isBefore(LocalDate.now()) || end.isBefore(start)) {
            throw new GroupException(Error.INVALID_DATE_RANGE);
        }

        return ResponseEntity.ok(groupService.searchByDate(start, end, pageable));
    }

    // 메뉴별 조회
    @GetMapping("/search/food")
    public ResponseEntity<Page<SearchedGroupDto>> searchByFood(@RequestParam List<String> foods,
                                                               Pageable pageable) {
        return ResponseEntity.ok(groupService.searchByFood(foods, pageable));
    }

    // 내 근처 모임
    @GetMapping("/near")
    public ResponseEntity<Page<NearbyGroupDto>> getNearbyGroupList(@RequestParam String latitude,
                                                                   @RequestParam String longitude,
                                                                   Pageable pageable) {
        return ResponseEntity.ok(groupService.getNearbyGroupList(latitude, longitude, pageable));
    }

    // 로그인한 사용자가 참여한 모임 조회
    @GetMapping("/accepted")
    public ResponseEntity<GroupDto.AcceptedGroup> getAcceptedGroupList(Authentication authentication) {
        return ResponseEntity.ok(groupService.getAcceptedGroupList(authentication));
    }

}
