package lk.tech.tgcontrollerbot.requests;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;

public interface HttpRequests {

    @PostExchange("api/v1/client/{key}")
    void send(@PathVariable String key, @RequestParam("command") String command);
}