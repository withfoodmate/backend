package com.foodmate.backend.controller;

import com.foodmate.backend.dto.GroupDto;
import com.foodmate.backend.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
