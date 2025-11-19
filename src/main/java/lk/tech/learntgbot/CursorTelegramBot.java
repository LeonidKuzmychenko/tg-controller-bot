package lk.tech.learntgbot;

import lk.tech.learntgbot.requests.HttpRequests;
import lk.tech.learntgbot.utils.KeyChatIdBiMap;
import lk.tech.learntgbot.utils.SendMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class CursorTelegramBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final HttpRequests httpRequests;
    private final KeyChatIdBiMap keyChatIdBiMap;

    public CursorTelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername, HttpRequests httpRequests, KeyChatIdBiMap keyChatIdBiMap
    ) {
        super(new DefaultBotOptions(), botToken);
        this.botUsername = botUsername;
        this.httpRequests = httpRequests;
        this.keyChatIdBiMap = keyChatIdBiMap;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                Long chatId = update.getMessage().getChatId();
                String text = update.getMessage().getText();
                log.info("onUpdateReceived chatId={}, text={}", chatId, text);
                if ("/start".equals(text)) {
                    SendMessage message = SendMessages.of(chatId, "Приветствую тебя в боте по управлению компьютером");
                    execute(message);
                    return;
                }
                if (text.startsWith("/")) {
                    String key = keyChatIdBiMap.getKeyByChatId(chatId);
                    httpRequests.send(key, text);
                }
            }
        } catch (Exception e) {
            // Все остальные ошибки (логика приложения, валидация и т.д.)
            log.error("Ошибка при обработке обновления", e);
        }
    }

}
