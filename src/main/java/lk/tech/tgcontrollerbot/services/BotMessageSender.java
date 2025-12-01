package lk.tech.tgcontrollerbot.services;

import lk.tech.tgcontrollerbot.dto.SendMessageRequest;
import lk.tech.tgcontrollerbot.requests.TelegramHttpClient;
import lk.tech.tgcontrollerbot.services.tg.TelegramMessageService;
import lk.tech.tgcontrollerbot.utils.TempFileManager;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.bots.AbsSender;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.nio.file.Path;
import java.util.function.BiFunction;

@Slf4j
@Service
public class BotMessageSender {

    private final UserDataService userDataService;
    private final TelegramMessageService telegramMessageService;

    public BotMessageSender(UserDataService userDataService, TelegramMessageService telegramMessageService) {
        this.telegramMessageService = telegramMessageService;
        this.userDataService = userDataService;
    }

    public Mono<Void> sendMessageToTG(String clientKey, String text) {
        log.info("sendMessageToTG invoked: clientKey={}, text={}", clientKey, text);

        return userDataService.getByClientKey(clientKey)
                .flatMap(user -> telegramMessageService.sendMessage(user.getChatId(), text)
                        .subscribeOn(Schedulers.boundedElastic())
                )
                .then();
    }


    public Mono<Void> sendRawPicturesWithCaption(
            String clientKey,
            Flux<FilePart> fileFlux,
            String caption
    ) {
        return userDataService.getByClientKey(clientKey)
                .flatMap(user ->
                        fileFlux.flatMapSequential(filePart ->
                                        TempFileManager.saveToTemp(filePart)
                                                .flatMap(tempPath ->
                                                        Mono.usingWhen(
                                                                // ресурс
                                                                Mono.just(tempPath),

                                                                // бизнес-логика
                                                                path -> telegramMessageService.sendDocument(
                                                                        user.getChatId(),
                                                                        path,
                                                                        caption
                                                                ),

                                                                // cleanup on success
                                                                path -> Mono.fromRunnable(() ->
                                                                        TempFileManager.safeDelete(path)
                                                                ),

                                                                (path, _) -> Mono.fromRunnable(() ->
                                                                        TempFileManager.safeDelete(path)),

                                                                // cleanup on cancel
                                                                path -> Mono.fromRunnable(() ->
                                                                        TempFileManager.safeDelete(path)
                                                                )
                                                        )
                                                ),
                                3 // max concurrency — оптимально, TelegramUploader сам ограничивает uploads
                        ).then()
                );
    }


}
