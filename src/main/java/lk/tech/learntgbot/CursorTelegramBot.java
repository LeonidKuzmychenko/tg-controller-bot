package lk.tech.learntgbot;

import lk.tech.learntgbot.senders.SocketMessageSender;
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
    private final SocketMessageSender socketMessageSender;
    private final KeyChatIdBiMap chatIdBiMap;

    public CursorTelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            SocketMessageSender socketMessageSender, KeyChatIdBiMap chatIdBiMap
    ) {
        super(new DefaultBotOptions(), botToken);
        this.botUsername = botUsername;
        this.socketMessageSender = socketMessageSender;
        this.chatIdBiMap = chatIdBiMap;
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
                if ("/start".equals(text)) {
                    SendMessage message = SendMessages.of(chatId, "Приветствую тебя в боте по управлению компьютером");
                    execute(message);
                }
                if ("/shutdown".equals(text)) {
                    socketMessageSender.sendToClient(chatIdBiMap.getKeyByChatId(chatId),"/shutdown");
                    SendMessage message = SendMessages.of(chatId, "PC был выключен");
                    execute(message);
                }
                if ("/screenshot".equals(text)) {
                    socketMessageSender.sendToClient(chatIdBiMap.getKeyByChatId(chatId),"/screenshot");
//                    SendMessage message = SendMessages.of(chatId, "PC был выключен");
//                    execute(message);
                }
            }
        } catch (Exception e) {
            // Все остальные ошибки (логика приложения, валидация и т.д.)
            log.error("Ошибка при обработке обновления", e);
        }
    }

}
