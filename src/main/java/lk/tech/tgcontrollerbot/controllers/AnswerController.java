package lk.tech.tgcontrollerbot.controllers;

import lk.tech.tgcontrollerbot.dto.ResultString;
import lk.tech.tgcontrollerbot.senders.BotMessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
    ) throws TelegramApiException {
        log.info("Received text request for  key={}, command={}, status={}", key, command, status);
        if ("Unknown".equals(status)){
            messageSender.sendMessageToTG(key, "Команды " + command + " не существует.\nСписок команда можно посмотреть вызвав /help");
            return;
        }
        if (!"Success".equals(status)){
            messageSender.sendMessageToTG(key, "Упс. Команду выполнить не удалось");
            return;
        }
        if ("/shutdown".equals(command)) {
            messageSender.sendMessageToTG(key, "PC был выключен");
        }
    }

    @PostMapping(value = "/object/{key}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void receiveObject(
            @PathVariable String key,
            @RequestParam String command,
            @RequestParam String status,
            @RequestBody ResultString result
    ) throws TelegramApiException {
        log.info("Received object request for  key={}, command={}, status={}", key, command, status);
        if ("Unknown".equals(status)){
            messageSender.sendMessageToTG(key, "Команды " + command + " не существует.\nСписок команда можно посмотреть вызвав /help");
            return;
        }
        if (!"Success".equals(status)){
            messageSender.sendMessageToTG(key, "Упс. Команду выполнить не удалось");
            return;
        }
        if ("/load".equals(command)) {
            messageSender.sendMessageToTG(key, "Нагрузка на пк:\n" + result.getData());
        }

    }

    @PostMapping(value = "/image/{key}", consumes = MediaType.IMAGE_PNG_VALUE)
    public void receiveImage(
            @PathVariable String key,
            @RequestParam String command,
            @RequestParam String status,
            @RequestBody byte[] bytes
    ) throws TelegramApiException {
        log.info("Received image request for  key={}, command={}, status={}", key, command, status);
        if ("Unknown".equals(status)){
            messageSender.sendMessageToTG(key, "Команды " + command + " не существует.\nСписок команда можно посмотреть вызвав /help");
            return;
        }
        if (!"Success".equals(status)){
            messageSender.sendMessageToTG(key, "Упс. Команду выполнить не удалось");
            return;
        }
        if ("/screenshot".equals(command)) {
            messageSender.sendRawPictureToTG(key, bytes, "Вот ваш скриншот");
        }
    }

}
