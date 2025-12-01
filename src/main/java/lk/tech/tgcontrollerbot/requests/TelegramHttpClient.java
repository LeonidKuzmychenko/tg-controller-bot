package lk.tech.tgcontrollerbot.requests;

import lk.tech.tgcontrollerbot.dto.SendMessageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

public interface TelegramHttpClient {

    // ----------------------------------------
    // SEND TEXT MESSAGE
    // ----------------------------------------
    @PostExchange(
            value = "/sendMessage",
            contentType = MediaType.APPLICATION_JSON_VALUE
    )
    Mono<String> sendText(@RequestBody SendMessageRequest body);

    // ----------------------------------------
    // SEND DOCUMENT (MULTIPART)
    // ----------------------------------------
//    @PostExchange(
//            value = "/sendDocument",
//            contentType = MediaType.MULTIPART_FORM_DATA_VALUE
//    )
//    Mono<String> sendDocument(
//            @RequestParam("chat_id") Long chatId,
//            @RequestParam(value = "caption", required = false) String caption,
//            @RequestPart("document") FileSystemResource document
//    );

    @PostExchange(
            value = "/sendDocument",
            contentType = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    Mono<String> sendDocument(@RequestPart MultiValueMap<String, HttpEntity<?>> parts);
}
