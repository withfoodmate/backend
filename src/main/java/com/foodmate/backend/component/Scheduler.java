package com.foodmate.backend.component;

import com.foodmate.backend.entity.ChatRoom;
import com.foodmate.backend.entity.FoodGroup;
import com.foodmate.backend.enums.EnrollmentStatus;
import com.foodmate.backend.enums.Error;
import com.foodmate.backend.exception.ChatException;
import com.foodmate.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Scheduler {

    private final FoodGroupRepository foodGroupRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
//    private final SimpMessageSendingOperations sendingOperations;

    @Transactional
    @Scheduled(cron = "0 0 5 * * *")
    public void processGroupCompletionTasks() {

        log.info("Start processing Group Completion Tasks.");

        LocalDateTime current = LocalDateTime.now();

        // 모임완료 처리할 푸드그룹 조회
        List<FoodGroup> groupList = foodGroupRepository.findAllByGroupDateTimeBetween(current.minusDays(1), current);

        for (FoodGroup foodGroup : groupList) {
            // 해당 모임의 수락된 Enrollment 상태를 모임완료로 일괄 변경
            enrollmentRepository.updateStatusToGroupCompleteByFoodGroupAndStatus(foodGroup, EnrollmentStatus.ACCEPT);

            ChatRoom chatRoom = chatRoomRepository.findByFoodGroupId(foodGroup.getId())
                    .orElseThrow(() -> new ChatException(Error.CHATROOM_NOT_FOUND));

            // 채팅방 구독 취소 요청
//            sendingOperations.convertAndSend("/topic/chatroom/" + chatRoom.getId(), "모임이 완료되었습니다.");

            // 채팅방 삭제 -> ChatMember, ChatMessage 먼저 삭제해야 채팅방 삭제 가능
            chatMemberRepository.deleteAllByChatRoom(chatRoom);
            chatMessageRepository.deleteAllByChatRoom(chatRoom);
            chatRoomRepository.delete(chatRoom);
        }

        log.info("Finished processing Group Completion Tasks.");
    }

}
