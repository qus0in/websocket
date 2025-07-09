package dev.qus0in.websocket.user.controller;

import dev.qus0in.websocket.user.dto.JoinRequestDTO;
import dev.qus0in.websocket.user.dto.UserResponseDTO;
import dev.qus0in.websocket.user.entity.Account;
import dev.qus0in.websocket.user.exception.DuplicateUsernameException;
import dev.qus0in.websocket.user.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping
@Slf4j
@RequiredArgsConstructor
public class UserController {
    @GetMapping
    String index(Model model, Authentication auth) {
        if (isAuthenticated(auth)) {
            model.addAttribute("accounts",
                    accountService.getAccounts().stream()
                            .map(el -> new UserResponseDTO(el.getUsername(), el.getId()))
                            .toList()
            );
        }
        return "index";
    }

    @GetMapping("login")
    String login(Authentication auth) {
        checkAnonymous(auth);
        return "login";
    }

    @GetMapping("join")
    String join(Authentication auth, Model model) {
        checkAnonymous(auth);
        model.addAttribute("account",
                new JoinRequestDTO("", ""));
        return "join";
    }

    private final AccountService accountService;

    @PostMapping("join")
    public String join(Authentication auth, JoinRequestDTO account, RedirectAttributes redirectAttributes) { // 3. throws 제거, RedirectAttributes 추가
        checkAnonymous(auth);
        try {
            // 컨트롤러는 DTO를 Entity로 변환하여 서비스에 전달
            accountService.createAccount(
                    Account.builder()
                            .username(account.username())
                            .password(account.password()) // 암호화되지 않은 원본 비밀번호 전달
                            .build());
        } catch (BadRequestException e) {
            // 유효성 검사 실패 (400 Bad Request)
            log.warn("회원가입 실패 (입력값 오류): {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/join";
        } catch (DuplicateUsernameException e) {
            // 사용자 이름 중복 (409 Conflict)
            log.warn("회원가입 실패 (사용자 이름 중복): {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/join";
        }

        // 성공 시 로그인 페이지로 이동
        return "redirect:/login";
    }

    private boolean isAuthenticated(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    private void checkAnonymous(Authentication auth) {
        if (isAuthenticated(auth)) {
            throw new NotAnonymousException(auth.getName());
        }
    }

    private static class NotAnonymousException extends RuntimeException {
        protected NotAnonymousException(String message) {
            super(message);
        }
    }

    @ExceptionHandler(NotAnonymousException.class)
    String notAnonymousExceptionHandler(NotAnonymousException e) {
        log.warn("로그인 상태로 접근 : {}", e.getMessage());
        return "redirect:/";
    }
}
