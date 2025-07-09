package dev.qus0in.websocket.user.service;

import dev.qus0in.websocket.user.entity.Account;
import dev.qus0in.websocket.user.exception.DuplicateUsernameException; // 1. Custom Exception 임포트
import dev.qus0in.websocket.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder; // 2. PasswordEncoder 임포트
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder; // 3. PasswordEncoder 주입

    @Transactional // 데이터 변경이 있으므로 트랜잭션 처리
    public Account createAccount(Account account) throws BadRequestException {
        if (account.getUsername().length() < 4) throw new BadRequestException("사용자 이름은 4자 이상이어야 합니다.");
        if (account.getPassword().length() < 8) throw new BadRequestException("비밀번호는 8자 이상이어야 합니다.");

        if (accountRepository.findByUsername(account.getUsername()).isPresent()) {
            throw new DuplicateUsernameException("이미 사용 중인 사용자 이름입니다.");
        }

        Account accountToSave = Account.builder()
                .username(account.getUsername())
                .password(passwordEncoder.encode(account.getPassword()))
                .build();

        log.debug("SUCCESS : {}", accountToSave.getUsername());
        return accountRepository.save(accountToSave);
    }

    public List<Account> getAccounts() {
        return accountRepository.findAll();
    }
}