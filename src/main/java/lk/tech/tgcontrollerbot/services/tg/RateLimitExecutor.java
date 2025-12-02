package lk.tech.tgcontrollerbot.services.tg;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.Semaphore;

@Component
public class RateLimitExecutor {

    private final Semaphore semaphore = new Semaphore(3, true);

    public <T> Mono<T> limitUpload(Mono<T> task) {
        return Mono.defer(() -> tryAcquire(task));
    }

    private <T> Mono<T> tryAcquire(Mono<T> task) {
        if (semaphore.tryAcquire()) {
            return task
                    .doFinally(sig -> semaphore.release());
        }

        // слот занят → пробуем снова через 10 мс
        return Mono.delay(Duration.ofMillis(10))
                .flatMap(i -> tryAcquire(task));
    }
}

