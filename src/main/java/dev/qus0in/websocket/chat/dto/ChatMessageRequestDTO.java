package dev.qus0in.websocket.chat.dto;

/**
 * 클라이언트에서 서버로 메시지를 보낼 때 사용하는 DTO
 * @param content 메시지 내용
 */
public record ChatMessageRequestDTO(String content) {
}