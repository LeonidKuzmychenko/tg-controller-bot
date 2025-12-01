package lk.tech.tgcontrollerbot.services.tg;

import lk.tech.tgcontrollerbot.dto.SendMessageRequest;
import lk.tech.tgcontrollerbot.requests.TelegramHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramMessageService {

    private final TelegramHttpClient http;
    private final TelegramMultipartBuilder multipart;
    private final RateLimitExecutor executor;
    private final TelegramErrorHandler errors;

    /* ------------------------- SEND TEXT ------------------------- */

    public Mono<String> sendMessage(Long chatId, String text) {
        SendMessageRequest req = new SendMessageRequest(chatId, text);

        return errors.applyRetry(
                        http.sendText(req)
                ).doOnNext(r -> log.info("[TG] sendMessage OK {}", chatId))
                .doOnError(e -> log.error("[TG] sendMessage ERR {} {}", chatId, e.getMessage()));
    }

    /* --------------------- SEND SINGLE PHOTO --------------------- */

//    public Mono<String> sendPhoto(Long chatId, Resource file, String caption) {
//        var parts = multipart.photo(chatId, file, caption);
//
//        return errors.applyRetry(
//                http.sendDocument(parts)
//        )
//        .doOnNext(r -> log.info("[TG] sendPhoto OK {}", file.getFilename()))
//        .doOnError(e -> log.error("[TG] sendPhoto ERR {} {}", file.getFilename(), e.getMessage()));
//    }

    /* --------------------- SEND SINGLE DOCUMENT ------------------ */

    public Mono<String> sendDocument(Long chatId, Path filePath, String caption) {

        MultiValueMap<String, HttpEntity<?>> parts = multipart.document(chatId, filePath, caption);

        return executor.limitUpload(
                        errors.applyRetry(
                                http.sendDocument(parts)
                        )
                )
                .doOnNext(r -> log.info("[TG] sendDocument OK {}", filePath.getFileName()))
                .doOnError(e -> log.error("[TG] sendDocument ERR {} {}", filePath.getFileName(), e.getMessage()));
    }

    /* ----------------------- SEND MEDIA GROUP -------------------- */

//    public Mono<String> sendMediaGroup(Long chatId, List<Resource> files) {
//        var parts = multipart.mediaGroup(chatId, files);
//
//        return executor.limitUpload(  // Telegram strongly recommends ≤2 media-group per second
//                errors.applyRetry(
//                        http.sendDocument(parts)
//                )
//        )
//        .doOnNext(r -> log.info("[TG] sendMediaGroup OK count={}", files.size()))
//        .doOnError(e -> log.error("[TG] sendMediaGroup ERR {}", e.getMessage()));
//    }
//
//    /* ----------------------- SEND LARGE FILE (>20MB) ------------- */
//    /** Telegram API requires: attach://fileWatermark */
//    public Mono<String> sendLargeFile(Long chatId, Path filePath, String caption) {
//        // логика идентична document, attach:// — уже внутри multipart.builder
//
//        Resource file = new FileSystemResource(filePath);
//        return sendDocument(chatId, filePath, caption)
//                .doOnNext(r -> log.info("[TG] LargeFile OK {}", file.getFilename()));
//    }
}
