package com.foodmate.backend.controller;

import com.foodmate.backend.dto.ChatDto;
import com.foodmate.backend.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/chatroom")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;


    @GetMapping()
    public ResponseEntity<List<ChatDto.ChatRoomListResponse>> getChatRoomList(Authentication authentication) {
        return ResponseEntity.ok(chatRoomService.getChatRoomList(authentication));
    }

    @GetMapping("/{chatRoomId}")
    public ResponseEntity<ChatDto.ChatRoomInfoResponse> getChatRoomInfo(@PathVariable Long chatRoomId){
        return ResponseEntity.ok(chatRoomService.getChatRoomInfo(chatRoomId));
    }

    @GetMapping("/{chatRoomId}/message")
    public ResponseEntity<ChatDto.ChatRoomMessageListResponse> getChatRoomMessageList(Authentication authentication, @PathVariable Long chatRoomId) {
        return ResponseEntity.ok(chatRoomService.getChatRoomMessageList(authentication ,chatRoomId));
    }
}
