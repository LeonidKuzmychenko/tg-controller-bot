package lk.tech.tgcontrollerbot.services;

import lk.tech.tgcontrollerbot.utils.SendMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BotMessageSender {

    private final AbsSender absSender;
    private final UserDataService userDataService;

    public BotMessageSender(AbsSender absSender, UserDataService userDataService) {
        log.debug("BotMessageSender: constructor called, absSender={}, userDataService={}", absSender, userDataService);
        this.absSender = absSender;
        this.userDataService = userDataService;
    }

    // -----------------------------------------------------------
    // TEXT MESSAGE
    // -----------------------------------------------------------

    public Mono<Void> sendMessageToTG(String clientKey, String text) {
        log.info("sendMessageToTG invoked: clientKey={}, text={}", clientKey, text);

        return userDataService.getByClientKey(clientKey)
                .doOnSubscribe(s -> log.debug("Fetching user by clientKey={}", clientKey))
                .doOnNext(user -> log.debug("User found: chatId={}", user.getChatId()))
                .flatMap(user -> Mono.fromRunnable(() -> {
                                    log.info("Sending TEXT to chatId={}, text={}", user.getChatId(), text);

                                    SendMessages.builder(user.getChatId())
                                            .text(text)
                                            .send(absSender);
                                })
                                .doOnSubscribe(s -> log.debug("Scheduling Telegram text send on boundedElastic"))
                                .doOnSuccess(v -> log.info("Text successfully sent to Telegram"))
                                .doOnError(e -> log.error("Error inside Telegram text send runnable", e))
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .doOnError(e -> log.error("sendMessageToTG FAILED: clientKey={}, text={}, error={}",
                        clientKey, text, e.toString(), e))
                .then();
    }

    // -----------------------------------------------------------
    // IMAGES
    // -----------------------------------------------------------

    public Mono<Void> sendRawPicturesWithCaption(String clientKey, Flux<FilePart> fileFlux, String text) {
        log.info("sendRawPicturesWithCaption invoked: clientKey={}, caption='{}'", clientKey, text);

        return userDataService.getByClientKey(clientKey)
                .doOnSubscribe(s -> log.debug("Fetching user by clientKey for images: {}", clientKey))
                .doOnNext(u -> log.debug("User found for images: chatId={}", u.getChatId()))
                .flatMap(user ->
                        fileFlux
                                .doOnSubscribe(s -> log.debug("Start reading fileFlux"))
                                .doOnNext(fp -> log.info("Incoming FilePart: filename={}, headers={}", fp.filename(), fp.headers()))
                                .flatMap(this::filePartToBytes)
                                .collectList()
                                .doOnSuccess(list -> log.info("Collected {} files from request", list.size()))
                                .flatMap(bytesList -> sendToTelegram(user.getChatId(), bytesList, text))
                )
                .doOnError(err -> log.error("sendRawPicturesWithCaption FAILED: clientKey={}, error={}", clientKey, err.toString(), err))
                .then();
    }

    // -----------------------------------------------------------
    // READ FILEPART TO BYTE[]
    // -----------------------------------------------------------
    private static final int CHUNK_SIZE = 16 * 1024;  // 16KB
    private static final int MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    private Mono<byte[]> filePartToBytes(FilePart filePart) {
        log.debug("filePartToBytes invoked for file: {}", filePart.filename());

        return filePart.content()
                .publishOn(Schedulers.boundedElastic())
                .reduce(new ByteArrayOutputStream(2 * CHUNK_SIZE), (baos, buffer) -> {
                    try {

                        int readable = buffer.readableByteCount();
                        if (readable == 0) {
                            return baos;
                        }

                        if (baos.size() + readable > MAX_FILE_SIZE) {
                            throw new IllegalStateException("File too large (limit 50MB)");
                        }

                        byte[] chunk = new byte[CHUNK_SIZE];

                        int remaining = readable;
                        while (remaining > 0) {
                            int toRead = Math.min(remaining, CHUNK_SIZE);

                            buffer.read(chunk, 0, toRead);
                            baos.write(chunk, 0, toRead);

                            remaining -= toRead;
                        }

                        log.trace("Read {} bytes from DataBuffer for file {}", readable, filePart.filename());

                    } catch (Exception e) {
                        log.error("Error processing DataBuffer for file {}: {}", filePart.filename(), e.toString(), e);
                        throw new RuntimeException(e);
                    } finally {
                        DataBufferUtils.release(buffer);
                    }

                    return baos;
                })
                .map(baos -> {
                    byte[] arr = baos.toByteArray();
                    log.info("File {} read successfully, totalBytes={}", filePart.filename(), arr.length);
                    return arr;
                })
                .doOnError(e -> log.error("filePartToBytes FAILED: file={}, error={}",
                        filePart.filename(), e.toString(), e));
    }




    // -----------------------------------------------------------
    // SEND IMAGES
    // -----------------------------------------------------------

    private Mono<Void> sendToTelegram(Long chatId, List<byte[]> bytesList, String text) {
        log.info("sendToTelegram invoked: chatId={}, images={}, caption='{}'", chatId, bytesList.size(), text);

        return Mono.fromRunnable(() -> {
                    log.debug("Preparing Telegram media list: chatId={}, images={}", chatId, bytesList.size());

                    List<InputMedia> mediaList = new ArrayList<>(bytesList.size());

                    for (int i = 0; i < bytesList.size(); i++) {
                        byte[] bytes = bytesList.get(i);

                        log.debug("Creating InputMediaDocument #{} ({} bytes)", i, bytes.length);

                        InputMediaDocument media = new InputMediaDocument();
                        media.setMedia(new ByteArrayInputStream(bytes), "screenshot_" + i + ".png");
                        mediaList.add(media);
                    }

                    try {
                        log.info("Sending CAPTION message to chatId={}", chatId);
                        absSender.execute(SendMessage.builder().chatId(chatId).text(text).build());

                        log.info("Sending {} images as MediaGroup to chatId={}", bytesList.size(), chatId);
                        absSender.execute(SendMediaGroup.builder().chatId(chatId).medias(mediaList).build());

                        log.info("Images successfully sent to Telegram chatId={}", chatId);

                    } catch (TelegramApiException e) {
                        log.error("Telegram API error while sending images to chatId={}: {}", chatId, e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                })
                .doOnSubscribe(s -> log.debug("Scheduling Telegram image send on boundedElastic"))
                .doOnError(e -> log.error("sendToTelegram FAILED: chatId={}, error={}", chatId, e.toString(), e))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
