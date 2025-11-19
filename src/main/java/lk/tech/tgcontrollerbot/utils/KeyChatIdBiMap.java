package lk.tech.tgcontrollerbot.utils;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class KeyChatIdBiMap {

    private final Map<String, Long> chatIdMap;
    private final Map<Long, String> keyMap;

    public KeyChatIdBiMap() {
        chatIdMap = new ConcurrentHashMap<>();
        keyMap = new ConcurrentHashMap<>();
        add("CLIENT_001", 346843164L);
    }

    public void add(String key, Long chatId) {
        chatIdMap.put(key, chatId);
        keyMap.put(chatId, key);
    }

    public Long getChatIdByKey(String key) {
        return chatIdMap.get(key);
    }

    public String getKeyByChatId(Long chatId) {
        return keyMap.get(chatId);
    }
}
