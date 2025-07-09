package dev.qus0in.websocket.chat.repository;

import dev.qus0in.websocket.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 특정 채팅방의 모든 메시지를 전송 시간 오름차순으로 조회합니다.
     * @param chatRoomId 채팅방의 ID
     * @return 메시지 목록
     */
    // 메서드 이름을 엔티티 필드 'createdAt'에 맞게 수정
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(UUID chatRoomId);
}