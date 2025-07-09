package dev.qus0in.websocket.chat.repository;

import dev.qus0in.websocket.chat.entity.ChatRoom;
import dev.qus0in.websocket.user.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
    /**
     * 두 명의 특정 참여자만으로 구성된 1:1 채팅방을 찾습니다.
     * @param user1 첫 번째 참여자
     * @param user2 두 번째 참여자
     * @return Optional<ChatRoom>
     */
    @Query("SELECT cr FROM ChatRoom cr WHERE :user1 MEMBER OF cr.participants AND :user2 MEMBER OF cr.participants AND SIZE(cr.participants) = 2")
    Optional<ChatRoom> findOneToOneChatRoom(@Param("user1") Account user1, @Param("user2") Account user2);
}