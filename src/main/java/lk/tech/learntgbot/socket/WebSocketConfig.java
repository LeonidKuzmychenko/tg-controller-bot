package lk.tech.learntgbot.socket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DesktopSocketHandler desktopSocketHandler;

    public WebSocketConfig(DesktopSocketHandler desktopSocketHandler) {
        this.desktopSocketHandler = desktopSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(desktopSocketHandler, "/ws")
                .setAllowedOrigins("*");
    }

    /**
     * 🔥 ВАЖНО: правильная конфигурация для увеличения бинарных лимитов
     */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();

        container.setMaxTextMessageBufferSize(20 * 1024 * 1024);    // 20MB
        container.setMaxBinaryMessageBufferSize(20 * 1024 * 1024);  // 20MB
        container.setMaxSessionIdleTimeout(0L);
        container.setAsyncSendTimeout(10_000L);

        return container;
    }
}
