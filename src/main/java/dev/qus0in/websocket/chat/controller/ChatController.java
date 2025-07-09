package dev.qus0in.websocket.chat.controller;

import dev.qus0in.websocket.chat.dto.ChatMessageResponseDTO;
import dev.qus0in.websocket.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    /**
     * '채팅하기' 버튼을 누르면 호출됩니다.
     * 기존 채팅방을 찾거나 새로 생성하여 해당 채팅방으로 리다이렉트합니다.
     * @param recipientUsername 상대방 사용자 이름
     * @param authentication 현재 인증 정보
     * @return 채팅방 URL로 리다이렉트
     */
    @PostMapping("/chat/start/{recipientUsername}")
    public String startChat(@PathVariable String recipientUsername, Authentication authentication) {
        String currentUsername = authentication.getName();

        UUID roomId = chatService.findOrCreateRoom(currentUsername, recipientUsername);
        return "redirect:/chat/" + roomId;
    }

    /**
     * 채팅방 페이지를 렌더링합니다.
     * @param roomId 채팅방 ID
     * @param model 뷰에 데이터를 전달할 모델
     * @param authentication 현재 인증 정보
     * @return chat.html 템플릿
     */
    @GetMapping("/chat/{roomId}")
    public String chatRoom(@PathVariable UUID roomId, Model model, Authentication authentication) {
        String currentUsername = authentication.getName();
        // 사용자가 이 채팅방의 참여자인지 확인합니다. (보안 강화)
        chatService.verifyRoomParticipant(roomId, currentUsername);

        log.info("Entering chat room: {}", roomId);

        List<ChatMessageResponseDTO> messages = chatService.findMessagesByRoomId(roomId);
        model.addAttribute("roomId", roomId);
        model.addAttribute("messages", messages);
        model.addAttribute("currentUser", authentication.getName()); // 현재 사용자 이름 추가

        return "chat"; // chat.html 템플릿을 반환
    }


    /**
     * 채팅방 나가기 요청을 처리합니다.
     * @param roomId 나갈 채팅방의 ID
     * @param authentication 현재 인증 정보
     * @return 메인 페이지("/")로 리다이렉트
     */
    @PostMapping("/chat/{roomId}/leave")
    public String leaveChatRoom(@PathVariable UUID roomId, Authentication authentication) {
        String currentUsername = authentication.getName();
        chatService.leaveRoom(roomId, currentUsername);
        log.info("User '{}' left chat room: {}", currentUsername, roomId);
        return "redirect:/";
    }
}