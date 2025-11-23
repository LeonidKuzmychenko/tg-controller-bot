package lk.tech.tgcontrollerbot;

import lk.tech.tgcontrollerbot.requests.HttpRequests;
import lk.tech.tgcontrollerbot.utils.KeyChatIdBiMap;
import lk.tech.tgcontrollerbot.utils.SendMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                    SendMessage message = SendMessages.of(chatId, "Приветствую тебя в боте по управлению компьютером\nСписок существующих команд можно посмотреть вызвав /help");
                    execute(message);
                    return;
                }
                if ("/help".equals(text)) {
                    Map<String,String> map = new HashMap<>();
                    map.put("/shutdown", "Выключить компьютер");
                    map.put("/screenshot", "Скриншот экрана");
                    map.put("/load", "Нагрузка ПК");
                    String result = Flux.fromIterable(map.entrySet())
                            .map(e -> e.getKey() + " - " + e.getValue())
                            .collect(Collectors.joining("\n"))
                            .block();

                    SendMessage message = SendMessages.of(chatId, "Список существующих команд:\n" + result);
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
