package com.foodmate.backend.component;

import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.WebSocketException;
import com.foodmate.backend.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations sendingOperations;
    private final ChatMessageService chatMessageService;

    // 연결 성공시
    @EventListener
    public void onSessionConnectedEvent(SessionConnectedEvent event) {
        log.info("연결 성공");
    }

    // 채팅방 구독
    @EventListener
    public void onSessionSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String nickname = getNickname(accessor);
        String destination = getDestination(accessor);

        log.info("구독자 닉네임 : {} / 채팅방 : {}", nickname, destination);

        String message = nickname + "님이 들어왔습니다.";
        sendingOperations.convertAndSend(destination, message);
        chatMessageService.incrementAttendance(destination, nickname);
    }

    // 채팅방 구독 취소
    @EventListener
    public void onSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String nickname = getNickname(accessor);
        String destination = getDestination(accessor);

        log.info("구독 취소자 닉네임 : {} / 채팅방 : {}", nickname, destination);

        String message = nickname + "님이 나갔습니다.";
        sendingOperations.convertAndSend(destination, message);
        chatMessageService.decrementAttendance(destination, nickname);
    }

    // 연결 종료시
    @EventListener
    public void onSessionDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String nickname = getNickname(accessor);

        log.info("연결 종료 닉네임 : {}", nickname);
    }

    private static String getNickname(StompHeaderAccessor accessor) {

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes == null) {
            throw new WebSocketException(Error.MISSING_SESSION_ATTRIBUTE);
        }

        return (String) accessor.getSessionAttributes().get("nickname");
    }

    private String getDestination(StompHeaderAccessor accessor) {

        String destination = accessor.getDestination();

        if (Objects.isNull(destination)) {
            throw new WebSocketException(Error.MISSING_DESTINATION);
        }

        return destination;
    }
}
