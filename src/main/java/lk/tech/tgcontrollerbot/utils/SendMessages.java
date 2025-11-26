package lk.tech.tgcontrollerbot.utils;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;


public class SendMessages {

    public static SendMessageBuilder builder(Long chatId) {
        return new SendMessageBuilder(chatId);
    }

    public static class SendMessageBuilder {

        private final Long chatId;
        private String text;
        private byte[] image;

        public SendMessageBuilder(Long chatId) {
            this.chatId = chatId;
        }

        public SendMessageBuilder text(String text) {
            this.text = text;
            return this;
        }

        public SendMessageBuilder image(byte[] image) {
            this.image = image;
            return this;
        }

        public void send(AbsSender absSender) {
            try {
                if (image == null) {
                    SendMessage message = new SendMessage();
                    message.setChatId(chatId);
                    message.setText(text);
                    message.setParseMode("HTML");
                    absSender.execute(message);
                    return;
                }
                SendDocument document = new SendDocument();
                document.setChatId(chatId);
                document.setCaption(text);
                document.setParseMode("HTML");

                InputFile file = new InputFile(
                        new ByteArrayInputStream(image),
                        "image.png"
                );

                document.setDocument(file);
                absSender.execute(document);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
