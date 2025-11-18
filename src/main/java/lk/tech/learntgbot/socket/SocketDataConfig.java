package lk.tech.learntgbot.socket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class SocketDataConfig {

    @Bean
    public Map<String, WebSocketSession> clients(){
        return new ConcurrentHashMap<>();
    }
}
