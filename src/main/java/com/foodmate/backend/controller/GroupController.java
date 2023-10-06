package com.foodmate.backend.controller;

import com.foodmate.backend.dto.CommentDto;
import com.foodmate.backend.dto.GroupDto;
import com.foodmate.backend.dto.ReplyDto;
import com.foodmate.backend.dto.SearchedGroupDto;
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
    public ResponseEntity<String> addGroup(Authentication authentication,
                                           @RequestBody @Valid GroupDto.Request request) {
        return ResponseEntity.ok(groupService.addGroup(authentication, request));
    }

    // 특정 모임 상세 조회
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDto.DetailResponse> getGroupDetail(@PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupDetail(groupId));
    }

    // 특정 모임 수정
    @PutMapping("/{groupId}")
    public ResponseEntity<String> updateGroup(@PathVariable Long groupId,
                                              Authentication authentication,
                                              @RequestBody @Valid GroupDto.Request request) {
        return ResponseEntity.ok(groupService.updateGroup(groupId, authentication, request));
    }

    // 특정 모임 삭제
    @DeleteMapping("/{groupId}")
    public ResponseEntity<String> deleteGroup(@PathVariable Long groupId,
                                              Authentication authentication) {
        return ResponseEntity.ok(groupService.deleteGroup(groupId, authentication));
    }

    // 특정 모임 신청
    @PostMapping("/{groupId}/enrollment")
    public ResponseEntity<String> enrollInGroup(@PathVariable Long groupId,
                                                Authentication authentication) {
        return ResponseEntity.ok(groupService.enrollInGroup(groupId, authentication));
    }

    // 댓글 작성
    @PostMapping("/{groupId}/comment")
    public ResponseEntity<String> addComment(@PathVariable Long groupId,
                                             Authentication authentication,
                                             @RequestBody @Valid CommentDto.Request request) {
        return ResponseEntity.ok(groupService.addComment(groupId, authentication, request));
    }

    // 대댓글 작성
    @PostMapping("/{groupId}/comment/{commentId}/reply")
    public ResponseEntity<String> addReply(@PathVariable Long groupId,
                                           @PathVariable Long commentId,
                                           Authentication authentication,
                                           @RequestBody @Valid ReplyDto.Request request) {
        return ResponseEntity.ok(groupService.addReply(groupId, commentId, authentication, request));
    }

    // 댓글 수정
    @PutMapping("/{groupId}/comment/{commentId}")
    public ResponseEntity<String> updateComment(@PathVariable Long groupId,
                                                @PathVariable Long commentId,
                                                Authentication authentication,
                                                @RequestBody @Valid CommentDto.Request request) {
        return ResponseEntity.ok(groupService.updateComment(groupId, commentId, authentication, request));
    }

    // 대댓글 수정
    @PutMapping("/{groupId}/comment/{commentId}/reply/{replyId}")
    public ResponseEntity<String> updateReply(@PathVariable Long groupId,
                                              @PathVariable Long commentId,
                                              @PathVariable Long replyId,
                                              Authentication authentication,
                                              @RequestBody @Valid ReplyDto.Request request) {
        return ResponseEntity.ok(groupService.updateReply(groupId, commentId, replyId, authentication, request));
    }

    // 댓글 삭제
    @DeleteMapping("/{groupId}/comment/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long groupId,
                                                @PathVariable Long commentId,
                                                Authentication authentication) {
        return ResponseEntity.ok(groupService.deleteComment(groupId, commentId, authentication));
    }

    // 대댓글 삭제
    @DeleteMapping("/{groupId}/comment/{commentId}/reply/{replyId}")
    public ResponseEntity<String> deleteReply(@PathVariable Long groupId,
                                              @PathVariable Long commentId,
                                              @PathVariable Long replyId,
                                              Authentication authentication) {
        return ResponseEntity.ok(groupService.deleteReply(groupId, commentId, replyId, authentication));
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
    @GetMapping("/All")
    public ResponseEntity<Page<SearchedGroupDto>> getAllGroupList(Pageable pageable) {
        return ResponseEntity.ok(groupService.getAllGroupList(pageable));
    }

    // 거리순 조회 & 내 근처 모임
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

}
