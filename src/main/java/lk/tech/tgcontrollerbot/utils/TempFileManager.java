package lk.tech.tgcontrollerbot.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
public class TempFileManager {

    /**
     * Создаёт временный файл и сохраняет туда FilePart реактивно.
     * Возвращает Path временного файла.
     */
    public static Mono<Path> saveToTemp(FilePart filePart) {
        return Mono.fromCallable(() -> {
                    Path temp = Files.createTempFile("", ".png");
                    log.info("Temp file created: {}", temp);
                    return temp;
                })
                .subscribeOn(Schedulers.boundedElastic()) // создание temp-файла = блокирующая операция
                .flatMap(tempPath ->
                        DataBufferUtils.write(
                                        filePart.content(),
                                        tempPath,
                                        StandardOpenOption.WRITE
                                )
                                .doOnError(e -> log.error("Failed writing {} → {}", tempPath.getFileName().toString(), tempPath, e))
                                .doOnSuccess(_ -> log.info("Saved FilePart {} to {}", tempPath.getFileName().toString(), tempPath))
                                .thenReturn(tempPath)
                );
    }

    /**
     * Безопасное удаление временного файла по Path.
     */
    public static void safeDelete(Path path) {
        try {
            Files.deleteIfExists(path);
            log.debug("Temp file deleted: {}", path);
        } catch (Exception e) {
            log.error("Failed to delete temp file {}", path, e);
        }
    }
}
