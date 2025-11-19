package lk.tech.tgcontrollerbot.controllers;

import lk.tech.tgcontrollerbot.dto.OrderData;
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

    @PostMapping(value = "/text/{key}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void receiveText(
            @PathVariable String key,
            @RequestParam String command,
            @RequestBody OrderData orderData
    ) throws TelegramApiException {
        log.info("Received text request for key={}, command={}", key, command);
        if ("/shutdown".equals(command)) {
            messageSender.sendMessageToTG(key, "PC был выключен");
        }
    }

    @PostMapping(value = "/image/{key}", consumes = MediaType.IMAGE_PNG_VALUE)
    public void receiveImage(
            @PathVariable String key,
            @RequestParam String command,
            @RequestBody byte[] bytes
    ) throws TelegramApiException {
        log.info("Received image request for key={}, command={}", key, command);
        if ("/screenshot".equals(command)) {
            messageSender.sendRawPictureToTG(key, bytes, "Вот ваш скриншот");
        }
    }

}
