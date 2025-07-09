package dev.qus0in.websocket;

import dev.qus0in.websocket.user.entity.Account;
import dev.qus0in.websocket.user.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableJpaAuditing
@SpringBootApplication
public class WebsocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsocketApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner userTest(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
//        return args -> {
//            if (accountRepository.findByUsername("mymy").isEmpty()) {
//                Account account = Account.builder()
//                        .username("mymy")
//                        .password(passwordEncoder.encode("youryour"))
//                        .build();
//                accountRepository.save(account);
//            }
//            if (accountRepository.findByUsername("hihi").isEmpty()) {
//                Account account = Account.builder()
//                        .username("hihi")
//                        .password(passwordEncoder.encode("byebyebye"))
//                        .build();
//                accountRepository.save(account);
//            }
//        };
//    }
}
