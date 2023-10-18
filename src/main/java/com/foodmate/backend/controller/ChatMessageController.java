package com.foodmate.backend.controller;

import com.foodmate.backend.dto.ChatMessageDto;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.WebSocketException;
import com.foodmate.backend.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/chatroom/{chatRoomId}/send")
    @SendTo("/topic/chatroom/{chatRoomId}")
    public ChatMessageDto.Response sendMessage(@DestinationVariable Long chatRoomId,
                                               @Payload ChatMessageDto.Request request,
                                               SimpMessageHeaderAccessor accessor) {
        Long memberId = getMemberId(accessor);

        log.info("발신자 아이디 : {} / 채팅방 : {}", memberId, chatRoomId);

        return chatMessageService.saveMessage(chatRoomId, memberId, request);
    }

    private Long getMemberId(SimpMessageHeaderAccessor accessor) {

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes == null) {
            throw new WebSocketException(Error.MISSING_SESSION_ATTRIBUTE);
        }

        return (Long) sessionAttributes.get("memberId");
    }

}
