package lk.tech.tgcontrollerbot.services;

import lk.tech.tgcontrollerbot.utils.SendMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BotMessageSender {

    private final AbsSender absSender;
    private final UserDataService userDataService;

    public BotMessageSender(AbsSender absSender, UserDataService userDataService) {
        this.absSender = absSender;
        this.userDataService = userDataService;
    }

    public Mono<Void> sendMessageToTG(String clientKey, String text) {
        return userDataService.getByClientKey(clientKey)
                .flatMap(user -> Mono.fromRunnable(() -> {
                                    log.info("Sending message to chatId={}, text={}", user.getChatId(), text);
                                    SendMessages.builder(user.getChatId())
                                            .text(text)
                                            .send(absSender);
                                })
                                .subscribeOn(Schedulers.boundedElastic()) // Telegram API — блокирующий
                )
                .doOnError(e -> log.error("Failed to send message", e))
                .then();
    }

    public Mono<Void> sendRawPicturesWithCaption(String clientKey, Flux<FilePart> fileFlux, String text) {
        return userDataService.getByClientKey(clientKey)
                .flatMap(user ->
                        fileFlux.flatMap(this::filePartToBytes)
                                .collectList()
                                .flatMap(bytesList -> sendToTelegram(user.getChatId(), bytesList, text))
                )
                .doOnError(err -> log.error("Failed to send pictures", err))
                .then();
    }

    private Mono<byte[]> filePartToBytes(FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
                .publishOn(Schedulers.boundedElastic())
                .handle((buffer, sink) -> {
                    try (var stream = buffer.asInputStream(true)) {
                        sink.next(stream.readAllBytes());
                    } catch (Exception e) {
                        sink.error(new RuntimeException("Failed to read filePart", e));
                    } finally {
                        DataBufferUtils.release(buffer);
                    }
                });
    }

    private Mono<Void> sendToTelegram(Long chatId, List<byte[]> bytesList, String text) {
        return Mono.fromRunnable(() -> {

                    log.info("Sending {} images to chatId={}", bytesList.size(), chatId);

                    List<InputMedia> mediaList = new ArrayList<>(bytesList.size());

                    for (int i = 0; i < bytesList.size(); i++) {
                        InputMediaDocument media = new InputMediaDocument();
                        media.setMedia(
                                new ByteArrayInputStream(bytesList.get(i)),
                                "screenshot_" + i + ".png"
                        );
                        mediaList.add(media);
                    }



                    try {
                        absSender.execute(SendMessage.builder().chatId(chatId).text(text).build());
                        absSender.execute(SendMediaGroup.builder().chatId(chatId).medias(mediaList).build());
                    } catch (TelegramApiException e) {
                        log.error("Telegram send failed", e);
                        throw new RuntimeException(e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
