package lk.tech.tgcontrollerbot.services;

import lk.tech.tgcontrollerbot.dto.SendMessageRequest;
import lk.tech.tgcontrollerbot.requests.TelegramHttpClient;
import lk.tech.tgcontrollerbot.utils.SendMessages;
import lk.tech.tgcontrollerbot.utils.TempFileManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.bots.AbsSender;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.util.function.Consumer;

@Slf4j
@Service
public class BotMessageSender {

    private final AbsSender absSender;
    private final UserDataService userDataService;
    private final TelegramHttpClient telegramService;

    public BotMessageSender(AbsSender absSender, UserDataService userDataService, TelegramHttpClient telegramService) {
        this.telegramService = telegramService;
        log.debug("BotMessageSender: constructor called, absSender={}, userDataService={}", absSender, userDataService);
        this.absSender = absSender;
        this.userDataService = userDataService;
    }

//    public Mono<Void> sendMessageToTG(String clientKey, String text) {
//        log.info("sendMessageToTG invoked: clientKey={}, text={}", clientKey, text);
//
//        return userDataService.getByClientKey(clientKey)
//                .doOnSubscribe(_ -> log.debug("Fetching user by clientKey={}", clientKey))
//                .doOnNext(user -> log.debug("User found: chatId={}", user.getChatId()))
//                .flatMap(user -> Mono.fromRunnable(() -> {
//                                    log.info("Sending TEXT to chatId={}, text={}", user.getChatId(), text);
//                                    SendMessages.builder(user.getChatId())
//                                            .text(text)
//                                            .send(absSender);
//
//                                    telegramService.sendText(user.getChatId(), text).subscribeOn(Schedulers.boundedElastic()).then();
//
//                                })
//                                .doOnSubscribe(_ -> log.debug("Scheduling Telegram text send on boundedElastic"))
//                                .doOnSuccess(_ -> log.info("Text successfully sent to Telegram"))
//                                .doOnError(e -> log.error("Error inside Telegram text send runnable", e))
//                                .subscribeOn(Schedulers.boundedElastic())
//                )
//                .doOnError(e -> log.error("sendMessageToTG FAILED: clientKey={}, text={}, error={}",
//                        clientKey, text, e.toString(), e))
//                .then();
//    }

    public Mono<Void> sendMessageToTG(String clientKey, String text) {
        log.info("sendMessageToTG invoked: clientKey={}, text={}", clientKey, text);

        return userDataService.getByClientKey(clientKey)
                .flatMap(user -> telegramService.sendText(new SendMessageRequest(user.getChatId(), text))
                        .subscribeOn(Schedulers.boundedElastic())
                )
                .then();
    }


    public Mono<Void> sendRawPicturesWithCaption(String clientKey,
                                                 Flux<FilePart> fileFlux,
                                                 String caption) {
        return userDataService.getByClientKey(clientKey)
                .flatMap(user -> fileFlux.flatMapSequential(filePart ->
                                TempFileManager.saveToTemp(filePart).flatMap(tempFile ->
                                        sendTelegramDocument(user.getChatId(), caption, tempFile)
                                                .doFinally(_ ->
                                                        TempFileManager.safeDelete(tempFile))), 1
                        ).then()
                );
    }

    private Mono<Void> sendTelegramDocument(Long chatId, String caption, File file) {

        MultipartBodyBuilder mb = new MultipartBodyBuilder();
        mb.part("chat_id", chatId);
        if (caption != null) {
            mb.part("caption", caption);
        }
        mb.part("document", new FileSystemResource(file));

        return telegramService.sendDocument(mb.build())
                .then();
    }


}
