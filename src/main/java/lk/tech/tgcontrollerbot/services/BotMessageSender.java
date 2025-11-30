package lk.tech.tgcontrollerbot.services;

import lk.tech.tgcontrollerbot.utils.SendMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class BotMessageSender {

    private final AbsSender absSender;
    private final UserDataService userDataService;

    public BotMessageSender(AbsSender absSender, UserDataService userDataService) {
        this.absSender = absSender;
        this.userDataService = userDataService;
    }

    /**
     * Отправить текстовое сообщение в Telegram по clientKey
     */
    public void sendMessageToTG(String clientKey, String text) {
        userDataService.getByClientKey(clientKey)
                .doOnNext(user -> {
                    Long chatId = user.getChatId();
                    log.info("Sending message to chatId={}, text={}", chatId, text);

                    SendMessages.builder(chatId)
                            .text(text)
                            .send(absSender);
                })
                .doOnError(e -> log.error("Failed to send message", e))
                .subscribe();
    }

    /**
     * Отправить изображение по clientKey
     */
    public void sendRawPictureToTG(String clientKey, byte[] pngBytes, String text) {
        userDataService.getByClientKey(clientKey)
                .doOnNext(user -> {
                    Long chatId = user.getChatId();
                    log.info("Sending file to chatId={}", chatId);

                    InputFile file = new InputFile(
                            new ByteArrayInputStream(pngBytes),
                            "image.png"
                    );

                    SendDocument doc = SendDocument.builder()
                            .chatId(chatId)
                            .caption(text)
                            .document(file)
                            .build();

                    try {
                        absSender.execute(doc);
                    } catch (TelegramApiException e) {
                        log.error("Failed to send document", e);
                    }
                })
                .doOnError(e -> log.error("Failed to send picture", e))
                .subscribe();
    }

    public Mono<Void> sendRawPicturesWithCaption(String clientKey, Flux<FilePart> fileFlux) {

        return userDataService.getByClientKey(clientKey)
                .flatMap(user ->
                        fileFlux.flatMap(this::filePartToBytes)      // FilePart → byte[]
                                .collectList()                       // собираем список byte[]
                                .flatMap(bytesList -> sendToTelegram(user.getChatId(), bytesList))
                )
                .doOnError(err -> log.error("Failed to send pictures", err))
                .then();
    }

    private Mono<byte[]> filePartToBytes(FilePart filePart) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        return filePart.content() // Flux<DataBuffer>
                .publishOn(Schedulers.boundedElastic()) // Flux<DataBuffer>
                .doOnNext(dataBuffer -> {
                    try {
                        Channels.newChannel(outputStream).write(dataBuffer.toByteBuffer());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .then(Mono.fromCallable(outputStream::toByteArray));
    }

    private Mono<Void> sendToTelegram(Long chatId, List<byte[]> bytesList) {

        return Mono.fromRunnable(() -> {
                    log.info("Sending {} images to chatId={}", bytesList.size(), chatId);

                    List<InputMedia> mediaList = new ArrayList<>();

                    for (int i = 0; i < bytesList.size(); i++) {
                        InputMediaDocument media = new InputMediaDocument();
                        media.setMedia(new ByteArrayInputStream(bytesList.get(i)),
                                "screenshot" + i + ".png");
                        mediaList.add(media);
                    }

                    SendMediaGroup group = new SendMediaGroup();
                    group.setChatId(chatId);
                    group.setMedias(mediaList);

                    try {
                        absSender.execute(group);
                    } catch (TelegramApiException e) {
                        log.error("Telegram send failed", e);
                        throw new RuntimeException(e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic()) // отправку Telegram делаем OFF main thread
                .then();
    }

//
//    /**
//     * Отправить несколько изображений по clientKey
//     */
//    public void sendRawPicturesWithCaption(String clientKey, Flux<FilePart> filePartFlux) {
//
//        List<FilePart> files = filePartFlux.toStream().toList();
//        userDataService.getByClientKey(clientKey)
//                .doOnNext(user -> {
//
//                    Long chatId = user.getChatId();
//                    log.info("Sending {} images to chatId={}", files.size(), chatId);
//
//                    // Получаем байты всех файлов
//                    List<byte[]> bytesList = files.stream()
//                            .flatMap(file -> {
//                                try {
//                                    return Stream.of(file.getBytes());
//                                } catch (IOException e) {
//                                    log.error("Failed to read file", e);
//                                    return Stream.empty();
//                                }
//                            })
//                            .toList();
//
//                    // Формируем Telegram media group
//                    List<InputMedia> mediaList = new ArrayList<>();
//                    for (int i = 0; i < bytesList.size(); i++) {
//                        InputMediaDocument media = new InputMediaDocument();
//                        media.setMedia(new ByteArrayInputStream(bytesList.get(i)),
//                                "screenshot" + i + ".png");
//                        mediaList.add(media);
//                    }
//
//                    SendMediaGroup group = new SendMediaGroup();
//                    group.setChatId(chatId);
//                    group.setMedias(mediaList);
//
//                    try {
//                        absSender.execute(group);
//                    } catch (TelegramApiException e) {
//                        log.error("Failed to send media group", e);
//                        throw new RuntimeException("Failed to send media group", e);
//                    }
//
//                })
//                .doOnError(e -> log.error("Failed to process sendRawPictures", e))
//                .subscribe();
//    }

}
