package com.foodmate.backend.component;

import com.foodmate.backend.entity.ChatRoom;
import com.foodmate.backend.entity.FoodGroup;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.enums.EnrollmentStatus;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.*;
import com.foodmate.backend.repository.ChatRoomRepository;
import com.foodmate.backend.repository.EnrollmentRepository;
import com.foodmate.backend.repository.MemberRepository;
import com.foodmate.backend.security.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private static final String PREFIX = "/topic/chatroom/";

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        StompCommand command = accessor.getCommand();

        if (StompCommand.CONNECT.equals(command)) {
            // JWT 인증
            Member member = getMemberFromAuthHeader(
                    accessor.getFirstNativeHeader("Authorization"));

            // 인증 완료후 세션에 사용자 정보 추가
            Map<String, Object> sessionAttributes = getSessionAttributes(accessor);
            sessionAttributes.put("memberId", member.getId());
            sessionAttributes.put("nickname", member.getNickname());

            log.info("연결 시도 : {}", member.getNickname());

        } else if (StompCommand.SUBSCRIBE.equals(command)) {

            Map<String, Object> sessionAttributes = getSessionAttributes(accessor);
            Long memberId = (Long) sessionAttributes.get("memberId");

            String destination = accessor.getDestination();
            Long chatRoomId = getChatRoomIdFromDestination(destination);

            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(() -> new ChatException(Error.CHATROOM_NOT_FOUND));

            // FoodGroup 검증 & 해당 멤버인지 검증
            isMemberInFoodGroup(memberId, chatRoom.getFoodGroup());

            log.info("검증 완료된 구독자 아이디 : {}", memberId);
        }

        return message;
    }

    private Member getMemberFromAuthHeader(String authHeader) {

        String accessToken = getAccessTokenFromAuthHeader(authHeader);

        Long memberId = jwtTokenProvider.extractId(accessToken)
                .orElseThrow(() -> new AuthException(Error.TOKEN_INVALID));

        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(Error.USER_NOT_FOUND));
    }

    private String getAccessTokenFromAuthHeader(String authHeader) {

        if (Objects.isNull(authHeader) || !authHeader.startsWith("Bearer ")) {
            throw new AuthException(Error.TOKEN_INVALID);
        }

        String accessToken = authHeader.substring("Bearer ".length()).trim();

        if (!jwtTokenProvider.isTokenValid(accessToken)) {
            throw new AuthException(Error.TOKEN_INVALID);
        }

        return accessToken;
    }

    private void isMemberInFoodGroup(Long memberId, FoodGroup foodGroup) {

        if (foodGroup.getIsDeleted() != null) {
            throw new GroupException(Error.GROUP_DELETED);
        }

        boolean exists = enrollmentRepository.existsByMemberIdAndFoodGroupAndStatus(
                memberId, foodGroup, EnrollmentStatus.ACCEPT);

        if (!(foodGroup.getMember().getId() == memberId || exists)) {
            throw new ChatException(Error.ACCESS_DENIED);
        }
    }

    private Map<String, Object> getSessionAttributes(StompHeaderAccessor accessor) {

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes == null) {
            throw new WebSocketException(Error.MISSING_SESSION_ATTRIBUTE);
        }

        return sessionAttributes;
    }

    private Long getChatRoomIdFromDestination(String destination) {

        if (Objects.isNull(destination)) {
            throw new WebSocketException(Error.MISSING_DESTINATION);
        }

        return Long.parseLong(destination.substring(PREFIX.length()));
    }

}
