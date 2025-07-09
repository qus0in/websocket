package dev.qus0in.websocket.user.service;

import dev.qus0in.websocket.user.entity.Account;
import dev.qus0in.websocket.user.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("loadUserByUsername");
        Optional<Account> accountOptional = accountRepository.findByUsername(username);
        if (accountOptional.isEmpty()) {
            log.warn("FAILED : {}", username);
            throw new UsernameNotFoundException("존재하지 않는 사용자");
        }
        Account account = accountOptional.get();
        log.debug("SUCCESS : {} {}", account.getId(), account.getUsername());
        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .roles("USER")
                .build();
    }
}
