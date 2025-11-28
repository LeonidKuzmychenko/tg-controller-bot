package lk.tech.tgcontrollerbot;

import lk.tech.tgcontrollerbot.model.UserData;
import lk.tech.tgcontrollerbot.model.UserState;
import lk.tech.tgcontrollerbot.requests.HttpRequests;
import lk.tech.tgcontrollerbot.services.UserDataCacheManager;
import lk.tech.tgcontrollerbot.utils.Commands;
import lk.tech.tgcontrollerbot.utils.SendMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CursorTelegramBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final HttpRequests httpRequests;
    private final UserDataCacheManager userDataCacheManager;

    public CursorTelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername, HttpRequests httpRequests, UserDataCacheManager userDataCacheManager
    ) {
        super(new DefaultBotOptions(), botToken);
        this.botUsername = botUsername;
        this.httpRequests = httpRequests;
        this.userDataCacheManager = userDataCacheManager;
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

                // Текущие данные пользователя
                UserData userData = userDataCacheManager.getOrCreate(chatId);
                String clientKey = userData.getClientKey();

                // ---------------------------
                // 1️⃣ Еcли ожидаем ключ — обрабатываем ключ
                // ---------------------------
                if (userData.getState() == UserState.WAITING_FOR_KEY && !text.startsWith("/")) {

                    clientKey = text.trim();

                    // Проверка простая: ключ должен иметь вид UUID или быть длиной > 16
                    boolean valid = isValidUUID(clientKey);

                    if (!valid) {
                        SendMessages.builder(chatId)
                                .text("Упс. Кажется это неправильный ключ\n" +
                                        "Попробуйте ещё раз — просто вставьте ключ из программы.")
                                .send(this);
                        return;
                    }

                    // Сбрасываем состояние
                    userDataCacheManager.updateState(chatId, clientKey, UserState.COMPLETED);

                    SendMessages.builder(chatId)
                            .text("Отлично! 🎉\nВаш компьютер успешно подключён.\n" +
                                    "Теперь можете использовать команды — список по /help")
                            .send(this);
                    return;
                }

                // --------------------------------
                // 2️⃣ Обычные команды
                // --------------------------------

                if ("/start".equals(text) && clientKey == null) {
                    SendMessages.builder(chatId)
                            .text("Приветствую тебя в боте по управлению компьютером\n" +
                                    "Для подключения бота к компьютеру необходимо скачать программу на Windows и подключить её с помощью команды /connect")
                            .send(this);
                    return;
                }

                if ("/start".equals(text)) {
                    SendMessages.builder(chatId)
                            .text("Ваш чат уже подключён к программе.\nСписок команд: /help")
                            .send(this);
                    return;
                }

                if ("/connect".equals(text)) {

                    // Ставим состояние WAITING_FOR_KEY
                    userDataCacheManager.updateState(chatId, null,  UserState.WAITING_FOR_KEY);

                    SendMessages.builder(chatId)
                            .text("После запуска Windows приложения нажмите по иконке в трее правой кнопкой и выберите «Скопировать ключ».\n\n" +
                                    "Затем просто вставьте ключ сюда (CTRL+V) и отправьте.")
                            .send(this);
                    return;
                }

                // Если нет ключа и это не команда /connect
                if (clientKey == null) {
                    SendMessages.builder(chatId)
                            .text("Ваш чат ещё не привязан ни к одному компьютеру.\n" +
                                    "Сначала выполните /connect")
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
                    httpRequests.send(clientKey, text);
                    return;
                }

                SendMessages.builder(chatId)
                        .text("Неизвестная команда.\nСписок команд: /help")
                        .send(this);

            }
        } catch (Exception e) {
            log.error("Ошибка при обработке обновления", e);
        }
    }

    public boolean isValidUUID(String key) {
        try {
            UUID.fromString(key); // выбросит IllegalArgumentException, если строка невалидная
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
