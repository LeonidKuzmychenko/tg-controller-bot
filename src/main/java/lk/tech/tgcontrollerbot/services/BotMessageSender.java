package lk.tech.tgcontrollerbot.services;

import lk.tech.tgcontrollerbot.model.UserData;
import lk.tech.tgcontrollerbot.utils.SendMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;

@Slf4j
@Service
public class BotMessageSender {

    private final AbsSender absSender;
    private final UserDataCacheManager userDataCacheManager;

    public BotMessageSender(AbsSender absSender, UserDataCacheManager userDataCacheManager) {
        this.absSender = absSender;
        this.userDataCacheManager = userDataCacheManager;
    }

    public void sendMessageToTG(String clientKey, String text) {
        Optional<UserData> byClientKey = userDataCacheManager.getByClientKey(clientKey);

        if (byClientKey.isPresent()) {
            UserData userData = byClientKey.get();
            Long chatId = userData.getChatId();
            log.info("Sending message to chatId={}, text={}", chatId, text);
            SendMessages.builder(chatId).text(text).send(absSender);
        }

    }

    public void sendRawPictureToTG(String clientKey, byte[] pngBytes, String text) {
        Optional<UserData> byClientKey = userDataCacheManager.getByClientKey(clientKey);

        if (byClientKey.isPresent()) {
            UserData userData = byClientKey.get();
            Long chatId = userData.getChatId();
            log.info("Sending file to chatId={}", chatId);
            SendMessages.builder(chatId).text(text).image(pngBytes).send(absSender);
        }
    }
}
