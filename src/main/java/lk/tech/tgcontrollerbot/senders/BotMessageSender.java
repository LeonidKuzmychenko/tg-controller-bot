package lk.tech.tgcontrollerbot.senders;

import lk.tech.tgcontrollerbot.utils.KeyChatIdBiMap;
import lk.tech.tgcontrollerbot.utils.SendMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
public class BotMessageSender {

    private final AbsSender absSender;
    private final KeyChatIdBiMap chatIdBiMap;

    public BotMessageSender(AbsSender absSender, KeyChatIdBiMap chatIdBiMap) {
        this.absSender = absSender;
        this.chatIdBiMap = chatIdBiMap;
    }

    public void sendMessageToTG(String clientKey, String text) {
        Long chatId = chatIdBiMap.getChatIdByKey(clientKey);

        log.info("Sending message to chatId={}, text={}", chatId, text);
        SendMessages.builder(chatId).text(text).send(absSender);
    }

    public void sendRawPictureToTG(String clientKey, byte[] pngBytes, String text) throws TelegramApiException {
        Long chatId = chatIdBiMap.getChatIdByKey(clientKey);

        log.info("Sending file to chatId={}", chatId);
        SendMessages.builder(chatId).text(text).image(pngBytes).send(absSender);
    }
}
