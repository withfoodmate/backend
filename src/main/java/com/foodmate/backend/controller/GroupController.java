package com.foodmate.backend.controller;

import com.foodmate.backend.dto.GroupDto;
import com.foodmate.backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

}
