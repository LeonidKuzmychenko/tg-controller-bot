package lk.tech.tgcontrollerbot.services.tg;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class TelegramErrorHandler {

    public <T> Mono<T> applyRetry(Mono<T> mono) {
        return mono
                .retryWhen(
                        Retry.backoff(3, Duration.ofSeconds(1))
                                .filter(err -> isRetriable(err))
                                .maxBackoff(Duration.ofSeconds(5))
                );
    }

    private boolean isRetriable(Throwable err) {
        String msg = err.getMessage();
        if (msg == null) return false;

        return msg.contains("429") ||
               msg.contains("Too Many Requests") ||
               msg.contains("5xx") ||
               msg.contains("502") ||
               msg.contains("503") ||
               msg.contains("504");
    }
}
