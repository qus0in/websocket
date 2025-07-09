package dev.qus0in.websocket.chat.service;

import dev.qus0in.websocket.chat.dto.ChatMessageResponseDTO;
import dev.qus0in.websocket.chat.entity.ChatMessage;
import dev.qus0in.websocket.chat.entity.ChatRoom;
import dev.qus0in.websocket.chat.repository.ChatMessageRepository;
import dev.qus0in.websocket.chat.repository.ChatRoomRepository;
import dev.qus0in.websocket.user.entity.Account;
import dev.qus0in.websocket.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AccountRepository accountRepository;

    /**
     * 두 사용자 간의 1:1 채팅방을 찾거나 새로 생성합니다.
     * @param currentUsername 현재 로그인한 사용자 이름
     * @param recipientUsername 상대방 사용자 이름
     * @return 채팅방의 ID
     */
    @Transactional
    public UUID findOrCreateRoom(String currentUsername, String recipientUsername) {
        Account currentUser = accountRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("현재 사용자를 찾을 수 없습니다: " + currentUsername));
        Account recipientUser = accountRepository.findByUsername(recipientUsername)
                .orElseThrow(() -> new UsernameNotFoundException("상대방 사용자를 찾을 수 없습니다: " + recipientUsername));

        // 기존에 두 사용자 간의 1:1 채팅방이 있는지 확인
        return chatRoomRepository.findOneToOneChatRoom(currentUser, recipientUser)
                .map(ChatRoom::getId) // 채팅방이 존재하면 ID를 반환
                .orElseGet(() -> { // 존재하지 않으면 새로 생성
                    Set<Account> participants = new HashSet<>();
                    participants.add(currentUser);
                    participants.add(recipientUser);

                    ChatRoom newChatRoom = ChatRoom.builder()
                            .participants(participants)
                            .build();
                    return chatRoomRepository.save(newChatRoom).getId();
                });
    }

    /**
     * 특정 채팅방의 모든 메시지를 조회합니다.
     * @param roomId 채팅방 ID
     * @return 메시지 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDTO> findMessagesByRoomId(UUID roomId) {
        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId).stream()
                .map(message -> new ChatMessageResponseDTO(
                        message.getSender().getUsername(),
                        message.getMessage(),
                        message.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 수신된 메시지를 DB에 저장하고, 브로드캐스팅할 응답 DTO를 생성하여 반환합니다.
     * @param roomIdStr 채팅방 ID (문자열)
     * @param senderUsername 메시지 보낸 사람
     * @param content 메시지 내용
     * @return ChatMessageResponseDTO
     */
    @Transactional
    public ChatMessageResponseDTO saveMessageAndGetResponse(String roomIdStr, String senderUsername, String content) {
        UUID roomId = UUID.fromString(roomIdStr);
        Account sender = accountRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + senderUsername));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));

        // 메시지를 생성하고 저장합니다.
        ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .message(content)
                .build();
        chatMessageRepository.save(chatMessage);

        // 저장된 메시지 정보를 바탕으로 응답 DTO를 생성하여 반환합니다.
        return new ChatMessageResponseDTO(
                chatMessage.getSender().getUsername(),
                chatMessage.getMessage(),
                chatMessage.getCreatedAt()
        );
    }

    // 보안 강화를 위한 참여자 확인 메서드 (ChatController에서 사용)
    @Transactional(readOnly = true)
    public void verifyRoomParticipant(UUID roomId, String username) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        boolean isParticipant = room.getParticipants().stream()
                .anyMatch(account -> account.getUsername().equals(username));
        if (!isParticipant) {
            throw new AccessDeniedException("이 채팅방에 참여할 권한이 없습니다.");
        }
    }

    /**
     * 사용자가 채팅방을 나갑니다.
     * @param roomId 나가려는 채팅방의 ID
     * @param username 나가는 사용자의 이름
     */
    @Transactional
    public void leaveRoom(UUID roomId, String username) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId));

        // Set에서 특정 사용자를 안전하게 제거합니다.
        // Account 엔티티에 equals/hashCode가 별도로 구현되지 않았을 수 있으므로, username으로 비교하여 제거하는 것이 가장 안전합니다.
        boolean removed = room.getParticipants().removeIf(participant -> participant.getUsername().equals(username));

        if (!removed) {
            // 이 경우는 거의 발생하지 않지만, 안전장치로 남겨둡니다.
            throw new AccessDeniedException("이 채팅방의 참여자가 아니므로 나갈 수 없습니다.");
        }

        // 선택: 채팅방에 아무도 남지 않았을 때의 처리
        // 현재는 기록 보존을 위해 방을 삭제하지 않고, 로그만 남깁니다.
        if (room.getParticipants().isEmpty()) {
            log.info("Chat room {} is now empty after user {} left.", roomId, username);
            // 만약 빈 채팅방과 메시지를 모두 삭제하고 싶다면 아래 주석을 해제하고 구현합니다.
            // chatMessageRepository.deleteAllByChatRoomId(roomId);
            // chatRoomRepository.delete(room);
        }
    }
}