package dev.qus0in.websocket.chat.entity;

import dev.qus0in.websocket.user.entity.Account;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) // 생성 시간 자동 관리
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

//    @Column(nullable = false)
//    private String name;

    // 채팅방 참여자 목록
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "chatroom_participants", // 중간 테이블
            joinColumns = @JoinColumn(name = "chat_room_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id")
    )
    private Set<Account> participants = new HashSet<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ChatRoom(Set<Account> participants) {
        this.participants = participants;
    }
}
