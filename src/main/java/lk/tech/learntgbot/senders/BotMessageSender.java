package lk.tech.learntgbot.senders;

import lk.tech.learntgbot.utils.KeyChatIdBiMap;
import lk.tech.learntgbot.utils.SendMessages;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class BotMessageSender {

    private final AbsSender absSender;
    private final KeyChatIdBiMap chatIdBiMap;

    public BotMessageSender(AbsSender absSender, KeyChatIdBiMap chatIdBiMap) {
        this.absSender = absSender;
        this.chatIdBiMap = chatIdBiMap;
    }

    public void sendMessageToTG(String clientKey, String text) throws TelegramApiException {
        Long chatId = chatIdBiMap.getChatIdByKey(clientKey);

        SendMessage message = SendMessages.of(chatId, text);
        absSender.execute(message);
    }

//    public void sendPictureToTG(String clientKey, String base64, String text) throws TelegramApiException {
//        Long chatId = chatIdBiMap.getChatIdByKey(clientKey);
//        SendPhoto message = SendMessages.photoBase64(chatId, base64, text);
//        absSender.execute(message);
//    }

    public void sendRawPictureToTG(String clientKey, byte[] pngBytes, String caption) throws TelegramApiException {
        Long chatId = chatIdBiMap.getChatIdByKey(clientKey);

        SendDocument message = SendMessages.file(chatId, pngBytes, caption);

        absSender.execute(message);
    }
}
