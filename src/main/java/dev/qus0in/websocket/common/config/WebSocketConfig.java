package dev.qus0in.websocket.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // STOMP 메시징
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 WebSocket에 연결할 때 사용할 엔드포인트를 등록합니다.
        // "/ws" 경로로 SockJS 연결을 허용합니다.
        // SockJS는 WebSocket을 지원하지 않는 브라우저에 대한 대체 옵션을 제공합니다.
        registry.addEndpoint("/ws").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // "/topic"으로 시작하는 경로를 구독하는 클라이언트에게 메시지를 브로드캐스팅합니다.
        registry.enableSimpleBroker("/topic");
        // "/app"으로 시작하는 메시지는 @MessageMapping 어노테이션이 붙은 메서드로 라우팅됩니다.
        registry.setApplicationDestinationPrefixes("/app");
    }
}
