package lk.tech.tgcontrollerbot;

import lk.tech.tgcontrollerbot.requests.HttpRequests;
import lk.tech.tgcontrollerbot.utils.Commands;
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
                String clientKey = keyChatIdBiMap.getKeyByChatId(chatId);

                if ("/start".equals(text) && clientKey == null) {
                    SendMessages.builder(chatId)
                            .text("Приветствую тебя в боте по управлению компьютером\nДля подключения бота к компьютеру необходимо скачать программу на Windows и подключить ее к боту с помощью команды /connect")
                            .send(this);
                    return;
                }

                if ("/start".equals(text)) {
                    SendMessages.builder(chatId)
                            .text("Приветствую тебя в боте по управлению компьютером.\nВаш чат уже подключен к необходимой программе.\nСписок существующих команд можно посмотреть вызвав /help")
                            .send(this);
                    return;
                }

                if ("/connect".equals(text)) {
                    SendMessages.builder(chatId)
                            .text("После запуска Windows приложения у вас должна была появиться соответсвующая иконка в панели Пуск.\nНажмите по ней правой кнопкой мыши и выберите пункт 'Копировать ключ'.\nПосле этого ключ появится в буфере обмена.\nВставьте его в этот чат комбинацией CTRL+V и отправьте следующим сообщением.")
                            .send(this);
                    return;
                }

                if (clientKey == null) {
                    SendMessages.builder(chatId)
                            .text("За вашим чатом еще не закреплен ни один компьютер.\nДля подключения бота к компьютеру необходимо скачать программу на Windows и подключить ее к боту с помощью команды /connect")
                            .send(this);
                    return;
                }

                if ("/help".equals(text)) {
                    Map<String, String> map = Commands.map();
                    String result = Flux.fromIterable(map.entrySet())
                            .map(e -> e.getKey() + " - " + e.getValue())
                            .collect(Collectors.joining("\n"))
                            .block();
                    SendMessages.builder(chatId)
                            .text("Список существующих команд:\n" + result)
                            .send(this);
                    return;
                }

                if (Commands.isExist(text)) {
                    SendMessages.builder(chatId)
                            .text("Мы получили вашу команду. Начинаем выполнение.")
                            .send(this);
                    String key = keyChatIdBiMap.getKeyByChatId(chatId);
                    httpRequests.send(key, text);
                    return;
                }

                SendMessages.builder(chatId)
                        .text("Введенной Вами команды не существует.\nСписок существующих команд можно посмотреть вызвав /help")
                        .send(this);

            }
        } catch (Exception e) {
            // Все остальные ошибки (логика приложения, валидация и т.д.)
            log.error("Ошибка при обработке обновления", e);
        }
    }

}
