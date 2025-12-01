package lk.tech.tgcontrollerbot.services.tg;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Semaphore;

@Component
public class RateLimitExecutor {

    private final Semaphore uploadSemaphore = new Semaphore(3, true); // 3 параллельных upload-а
    private final Scheduler uploadScheduler;

    public RateLimitExecutor() {
        this.uploadScheduler = Schedulers.newBoundedElastic(
                3,
                30,
                "tg-upload"
        );
    }

    public <T> Mono<T> limitUpload(Mono<T> task) {
        return Mono.fromCallable(() -> {
            uploadSemaphore.acquire();
            return true;
        })
        .flatMap(i -> task)
        .doFinally(sig -> uploadSemaphore.release())
        .subscribeOn(uploadScheduler);
    }
}
