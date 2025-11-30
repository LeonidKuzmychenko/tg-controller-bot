package lk.tech.tgcontrollerbot.services;

import lk.tech.tgcontrollerbot.model.UserData;
import lk.tech.tgcontrollerbot.utils.SendMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

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

            InputFile file = new InputFile(
                    new ByteArrayInputStream(pngBytes),
                    "image.png"
            );
            SendDocument build = SendDocument.builder().chatId(chatId).caption(text).document(file).build();
            try {
                absSender.execute(build);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendRawPicturesWithCaption(String clientKey, MultiValueMap<String, MultipartFile> files) {
        Optional<UserData> byClientKey = userDataCacheManager.getByClientKey(clientKey);

        if (byClientKey.isEmpty()) {
            log.warn("No user by key={}", clientKey);
            return;
        }

        Long chatId = byClientKey.get().getChatId();
        log.info("Sending {} images with caption to chatId={}", files.size(), chatId);

        List<byte[]> list = files.values()
                .stream()
                .flatMap(Collection::stream)
                .flatMap(it -> {
                    try {
                        return Stream.of(it.getBytes());
                    } catch (IOException e) {
                        return Stream.empty();
                    }
                }).toList();

        List<InputMedia> mediaList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            InputMediaDocument media = new InputMediaDocument();
            media.setMedia(new ByteArrayInputStream(list.get(i)), "screenshot"+i+".png");
            mediaList.add(media);
        }

        SendMediaGroup group = new SendMediaGroup();
        group.setChatId(chatId);
        group.setMedias(mediaList);

        try {
            absSender.execute(group);
        } catch (TelegramApiException e) {
            log.error("Failed to send media group", e);
            throw new RuntimeException("Failed to send media group", e);
        }
    }


}
