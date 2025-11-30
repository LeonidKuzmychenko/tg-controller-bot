package lk.tech.tgcontrollerbot.controllers;

import lk.tech.tgcontrollerbot.dto.ResultString;
import lk.tech.tgcontrollerbot.services.BotMessageSender;
import lk.tech.tgcontrollerbot.utils.Commands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/answer")
public class AnswerController {

    private final BotMessageSender messageSender;

    @PostMapping(value = "/text/{key}")
    public void receiveText(
            @PathVariable String key,
            @RequestParam String command,
            @RequestParam String status
    ) {
//        log.info("Received text request for  key={}, command={}, status={}", key, command, status);
        String description = Commands.getDescription(command);
        if ("Unknown".equals(status) || description == null){
            messageSender.sendMessageToTG(key, "Команды " + command + " не существует.\nСписок команда можно посмотреть вызвав /help");
            return;
        }
        if (!"Success".equals(status)){
            messageSender.sendMessageToTG(key, "Упс. Команду выполнить не удалось");
            return;
        }
        messageSender.sendMessageToTG(key, description);
    }

    @PostMapping(value = "/object/{key}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void receiveObject(
            @PathVariable String key,
            @RequestParam String command,
            @RequestParam String status,
            @RequestBody(required = false) ResultString result
    ) {
//        log.info("Received object request for  key={}, command={}, status={}", key, command, status);
        String description = Commands.getDescription(command);
        if ("Unknown".equals(status) || description == null){
            messageSender.sendMessageToTG(key, "Команды " + command + " не существует.\nСписок команда можно посмотреть вызвав /help");
            return;
        }
        if (!"Success".equals(status)){
            messageSender.sendMessageToTG(key, "Упс. Команду выполнить не удалось");
            return;
        }
        messageSender.sendMessageToTG(key, description + ":\n" + result.getData());
    }

    @PostMapping(
            value = "/images/{key}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Mono<Void> receiveImages(
            @PathVariable String key,
            @RequestParam String command,
            @RequestParam String status,
            @RequestPart("files") Flux<FilePart> files
    ) {
//        log.info("Received {} images for key={}, command={}, status={}",
//                files == null ? 0 : files.size(), key, command, status);

        if ("Unknown".equals(status)) {
            messageSender.sendMessageToTG(
                    key,
                    "Команды " + command + " не существует.\nСписок команд можно посмотреть вызвав /help"
            );
            return Mono.empty();
        }

        if (!"Success".equals(status) || files == null) {
            messageSender.sendMessageToTG(key, "Упс. Команду выполнить не удалось");
            return Mono.empty();
        }

        if ("/screenshot".equals(command)) {
            return messageSender.sendRawPicturesWithCaption(key, files);
        }

        return Mono.empty();
    }



}
