package lk.tech.tgcontrollerbot.controllers;

import lk.tech.tgcontrollerbot.dto.ResultString;
import lk.tech.tgcontrollerbot.services.BotMessageSender;
import lk.tech.tgcontrollerbot.utils.Commands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/answer")
public class AnswerController {

    private final BotMessageSender messageSender;

    // -------------------- TEXT --------------------
    @PostMapping("/{key}")
    public Mono<Void> receiveText(
            @PathVariable String key,
            @RequestParam String command,
            @RequestParam String status
    ) {
        return preCheck(key, command, status)
                .flatMap(description ->
                        messageSender.sendMessageToTG(key, description)
                );
    }

    // -------------------- JSON --------------------
    @PostMapping(value = "/{key}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Void> receiveObject(
            @PathVariable String key,
            @RequestParam String command,
            @RequestParam String status,
            @RequestBody ResultString result
    ) {
        return preCheck(key, command, status)
                .flatMap(description ->
                        messageSender.sendMessageToTG(key, description + ":\n" + result.getData())
                );
    }

    // -------------------- IMAGES --------------------
    @PostMapping(value = "/{key}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Void> receiveImages(
            @PathVariable String key,
            @RequestParam String command,
            @RequestParam String status,
            @RequestPart("files") Flux<FilePart> files
    ) {
        return preCheck(key, command, status)
                .flatMap(description ->
                        messageSender.sendRawPicturesWithCaption(key, files, description + ":")
                );
    }

    // ==================================================================
    //                  ОБЩАЯ ЛОГИКА ПРОВЕРКИ команд/статуса
    // ==================================================================
    private Mono<String> preCheck(String key, String command, String status) {
        String description = Commands.getDescription(command);

        if (description == null || "Unknown".equals(status)) {
            return messageSender
                    .sendMessageToTG(key,
                            "Команды " + command + " не существует.\nСписок команда можно посмотреть вызвав /help")
                    .then(Mono.empty());
        }

        if (!"Success".equals(status)) {
            return messageSender
                    .sendMessageToTG(key, "Упс. Команду выполнить не удалось")
                    .then(Mono.empty());
        }

        return Mono.just(description);
    }
}
