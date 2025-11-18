package lk.tech.learntgbot.utils;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;


public class SendMessages {

    public static SendMessage of(Long chatId, String text){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode("HTML");
        return message;
    }

//    public static SendPhoto photoBase64(Long chatId, String base64, String text) {
//        // 1. Декодируем Base64 → byte[]
//        byte[] pngBytes = Base64.getDecoder().decode(base64);
//
//        // 2. Формируем SendPhoto
//        SendPhoto photo = new SendPhoto();
//        photo.setChatId(chatId);
//        photo.setCaption(text);
//        photo.setParseMode("HTML");
//
//        // 3. Передаём как файл из памяти
//        InputFile input = new InputFile(
//                new ByteArrayInputStream(pngBytes),
//                "image.png"
//        );
//
//        photo.setPhoto(input);
//
//        return photo;
//    }

    public static SendPhoto photo(Long chatId, byte[] pngBytes, String caption) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setCaption(caption);
        photo.setParseMode("HTML");

        InputFile file = new InputFile(
                new ByteArrayInputStream(pngBytes),
                "image.png"
        );

        photo.setPhoto(file);
        return photo;
    }

    public static SendDocument file(Long chatId, byte[] pngBytes, String caption) {
        SendDocument document = new SendDocument();
        document.setChatId(chatId);
        document.setCaption(caption);
        document.setParseMode("HTML");

        InputFile file = new InputFile(
                new ByteArrayInputStream(pngBytes),
                "image.png"
        );

        document.setDocument(file);
        return document;
    }
}
