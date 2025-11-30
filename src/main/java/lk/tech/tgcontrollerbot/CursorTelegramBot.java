package lk.tech.tgcontrollerbot;

import lk.tech.tgcontrollerbot.model.UserData;
import lk.tech.tgcontrollerbot.model.UserState;
import lk.tech.tgcontrollerbot.requests.HttpRequests;
import lk.tech.tgcontrollerbot.services.UserDataService;
import lk.tech.tgcontrollerbot.utils.Commands;
import lk.tech.tgcontrollerbot.utils.SendMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CursorTelegramBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final HttpRequests httpRequests;
    private final UserDataService userDataService;

    public CursorTelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            HttpRequests httpRequests,
            UserDataService userDataService
    ) {
        super(new DefaultBotOptions(), botToken);
        this.botUsername = botUsername;
        this.httpRequests = httpRequests;
        this.userDataService = userDataService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText().trim();

        log.info("Bot Request: chatId={}, text={}", chatId, text);

        userDataService.getByChatId(chatId)
                .switchIfEmpty(
                        userDataService.createNew(chatId) // автоматическое создание новой записи
                )
                .flatMap(userData -> processMessage(chatId, text, userData))
                .onErrorResume(e -> {
                    log.error("Ошибка при обработке сообщения", e);
                    SendMessages.builder(chatId)
                            .text("Произошла ошибка. Попробуйте позже.")
                            .send(this);
                    return Mono.empty();
                })
                .subscribe();
    }

    private Mono<Void> processMessage(Long chatId, String text, UserData userData) {
        String clientKey = userData.getClientKey();
        UserState state = userData.getState();

        // ---------------------------
        // 1️⃣ Если ожидаем ключ
        // ---------------------------
        if (state == UserState.WAITING_FOR_KEY && !text.startsWith("/")) {

            String newKey = text.trim();

            if (!isValidUUID(newKey)) {
                SendMessages.builder(chatId)
                        .text("Упс. Похоже это неправильный ключ.\nПопробуйте ещё раз.")
                        .send(this);
                return Mono.empty();
            }

            return userDataService.updateState(chatId, newKey, UserState.COMPLETED)
                    .doOnSuccess(u ->
                            SendMessages.builder(chatId)
                                    .text("Отлично! 🎉 Ваш компьютер подключён.\nТеперь команды доступны: /help")
                                    .send(this)
                    )
                    .then();
        }

        // ---------------------------
        // 2️⃣ Команда /start
        // ---------------------------
        if ("/start".equals(text)) {

            if (clientKey == null) {
                SendMessages.builder(chatId)
                        .text("Приветствую!\nЧтобы привязать бота к компьютеру, выполните /connect")
                        .send(this);
            } else {
                SendMessages.builder(chatId)
                        .text("Вы уже подключены.\nКоманды: /help")
                        .send(this);
            }
            return Mono.empty();
        }

        // ---------------------------
        // 3️⃣ Команда /connect
        // ---------------------------
        if ("/connect".equals(text)) {

            return userDataService.updateState(chatId, null, UserState.WAITING_FOR_KEY)
                    .doOnSuccess(u ->
                            SendMessages.builder(chatId)
                                    .text("""
                                            После запуска Windows приложения:
                                            1️⃣ Нажмите правой кнопкой на иконку в трее
                                            2️⃣ Выберите «Копировать ключ»
                                            3️⃣ Отправьте ключ следующим сообщением в этот чат""")
                                    .send(this)
                    )
                    .then();
        }

        // ---------------------------
        // 4️⃣ Если нет clientKey — только /connect доступно
        // ---------------------------
        if (clientKey == null) {
            SendMessages.builder(chatId)
                    .text("Ваш чат ещё не привязан.\nСначала выполните /connect")
                    .send(this);
            return Mono.empty();
        }

        // ---------------------------
        // 5️⃣ /help
        // ---------------------------
        if ("/help".equals(text)) {
            Map<String, String> map = Commands.map();
            String result = map.entrySet().stream()
                    .map(e -> e.getKey() + " - " + e.getValue())
                    .collect(Collectors.joining("\n"));

            SendMessages.builder(chatId)
                    .text("Список команд:\n" + result)
                    .send(this);
            return Mono.empty();
        }

        // ---------------------------
        // 6️⃣ Команды приложения
        // ---------------------------
        if (Commands.isExist(text)) {
            SendMessages.builder(chatId)
                    .text("Команда получена. Выполняем…")
                    .send(this);

            return httpRequests.send(clientKey, text).then();
        }

        // ---------------------------
        // 7️⃣ Неизвестная команда
        // ---------------------------
        SendMessages.builder(chatId)
                .text("Неизвестная команда.\nСписок: /help")
                .send(this);
        return Mono.empty();
    }

    public boolean isValidUUID(String key) {
        try {
            UUID.fromString(key);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
