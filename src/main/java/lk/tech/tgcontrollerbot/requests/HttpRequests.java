package lk.tech.tgcontrollerbot.requests;

import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;

public interface HttpRequests {

    @PostExchange("api/v1/client/{key}")
    Mono<Void> send(@PathVariable String key, @RequestParam("command") String command);
}
