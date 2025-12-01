package lk.tech.tgcontrollerbot.services.tg;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.nio.file.Path;

@Component
public class TelegramMultipartBuilder {

//    public MultiValueMap<String, HttpEntity<?>> photo(
//            Long chatId,
//            Resource file,
//            String caption
//    ) {
//        MultipartBodyBuilder b = new MultipartBodyBuilder();
//
//        b.part("chat_id", chatId.toString());
//        if (caption != null) b.part("caption", caption);
//
//        b.part("photo", file)
//                .filename(file.getFilename())
//                .contentType(MediaType.IMAGE_JPEG);
//
//        return b.build();
//    }

    public MultiValueMap<String, HttpEntity<?>> document(
            Long chatId,
            Path filePath,
            String caption
    ) {
        MultipartBodyBuilder b = new MultipartBodyBuilder();

        b.part("chat_id", chatId.toString());
        if (caption != null) b.part("caption", caption);

        Resource fileResource = new FileSystemResource(filePath);

        b.part("document", fileResource)
                .filename(filePath.getFileName().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM);

        return b.build();
    }

//    /** MediaGroup для фото/видео */
//    public MultiValueMap<String, HttpEntity<?>> mediaGroup(
//            Long chatId,
//            List<Resource> files
//    ) {
//        MultipartBodyBuilder b = new MultipartBodyBuilder();
//
//        b.part("chat_id", chatId.toString());
//
//        List<Map<String, Object>> media = new ArrayList<>();
//
//        for (int i = 0; i < files.size(); i++) {
//            String attachName = "file" + i;
//
//            media.add(Map.of(
//                    "type", "photo",
//                    "media", "attach://" + attachName
//            ));
//
//            b.part(attachName, files.get(i))
//                    .filename(files.get(i).getFilename())
//                    .contentType(MediaType.APPLICATION_OCTET_STREAM);
//        }
//
//        b.part("media", media);
//
//        return b.build();
//    }
}
