package lk.tech.tgcontrollerbot.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
public class TempFileManager {

    public static Mono<File> saveToTemp(FilePart filePart) {
        return Mono.fromCallable(() -> {
                    Path temp = Files.createTempFile("upload_", ".png");
                    log.info("Temp file created: {}", temp);
                    return temp.toFile();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(tempFile ->
                        DataBufferUtils.write(
                                        filePart.content(),
                                        tempFile.toPath(),
                                        StandardOpenOption.WRITE
                                )
                                .doOnError(e -> log.error("Failed writing {} → {}", filePart.filename(), tempFile, e))
                                .doOnSuccess(_ -> log.info("Saved FilePart {} to {}", filePart.filename(), tempFile))
                                .thenReturn(tempFile)
                );
    }

    public static void safeDelete(File file) {
        try {
            Files.deleteIfExists(file.toPath());
            log.debug("Temp file deleted: {}", file);
        } catch (Exception e) {
            log.error("Failed to delete temp file {}", file, e);
        }
    }
}
