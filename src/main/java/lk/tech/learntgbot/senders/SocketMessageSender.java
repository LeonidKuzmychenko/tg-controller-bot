package lk.tech.learntgbot.senders;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;

@Service
public class SocketMessageSender {

    private final Map<String, WebSocketSession> clients;

    public SocketMessageSender(@Lazy Map<String, WebSocketSession> clients) {
        this.clients = clients;
    }

    public void sendToClient(String clientKey, String msg) throws IOException {
        WebSocketSession session = clients.get(clientKey);

        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(msg));
        } else {
            System.out.println("Client not connected: " + clientKey);
        }
    }
}
