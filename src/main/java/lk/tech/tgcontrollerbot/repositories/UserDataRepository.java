package lk.tech.tgcontrollerbot.repositories;

import lk.tech.tgcontrollerbot.model.UserData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserDataRepository extends ReactiveCrudRepository<UserData, Long> {
    Mono<UserData> findByClientKey(String clientKey);
    Mono<UserData> findByChatId(Long chatId);
}