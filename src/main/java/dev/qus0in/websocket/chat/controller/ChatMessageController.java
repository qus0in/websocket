package dev.qus0in.websocket.chat.controller;

import dev.qus0in.websocket.chat.dto.ChatMessageRequestDTO;
import dev.qus0in.websocket.chat.dto.ChatMessageResponseDTO;
import dev.qus0in.websocket.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatService chatService;

    /**
     * 클라이언트가 메시지를 보낼 때 사용하는 엔드포인트입니다.
     * /app/chat/{roomId} 경로로 메시지를 보내면 이 메서드가 처리합니다.
     * @param roomId 채팅방 ID
     * @param messageRequest 수신된 메시지 DTO
     * @param principal 현재 인증된 사용자 정보
     * @return 처리된 메시지를 /topic/chat/{roomId}를 구독하는 클라이언트들에게 브로드캐스팅합니다.
     */
    @MessageMapping("/chat/{roomId}") // 메시지 수신 경로
    @SendTo("/topic/chat/{roomId}")   // 처리 후 메시지 발행 경로
    public ChatMessageResponseDTO sendMessage(
            @DestinationVariable String roomId,
            ChatMessageRequestDTO messageRequest,
            Principal principal
    ) {
        String username = principal.getName();
        // 서비스를 호출하여 메시지를 저장하고, 응답 DTO를 반환받습니다.
        return chatService.saveMessageAndGetResponse(roomId, username, messageRequest.content());
    }
}