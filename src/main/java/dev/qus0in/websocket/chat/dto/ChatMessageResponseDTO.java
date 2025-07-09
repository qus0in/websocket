package dev.qus0in.websocket.chat.dto;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * 채팅 내역을 클라이언트에 전달할 때 사용할 DTO
 * @param sender 보낸 사람
 * @param message 메시지 내용
 * @param sentAt 보낸 시간
 */
public record ChatMessageResponseDTO(
        String sender,
        String message,
        LocalDateTime sentAt
) {}
