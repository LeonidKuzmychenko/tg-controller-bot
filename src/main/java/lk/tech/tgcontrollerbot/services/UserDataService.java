package lk.tech.tgcontrollerbot.services;

import lk.tech.tgcontrollerbot.model.UserData;
import lk.tech.tgcontrollerbot.model.UserState;
import lk.tech.tgcontrollerbot.repositories.UserDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserDataService {

    private final UserDataRepository repo;

    /**
     * Найти пользователя по chat_id
     */
    public Mono<UserData> getByChatId(Long chatId) {
        return repo.findByChatId(chatId);
    }

    /**
     * Создать нового пользователя
     */
    public Mono<UserData> createNew(Long chatId) {
        return repo.save(
                UserData.builder()
                        .chatId(chatId)
                        .state(UserState.IDLE)
                        .build()
        );
    }

    /**
     * Найти или создать
     */
    public Mono<UserData> getOrCreate(Long chatId) {
        return getByChatId(chatId)
                .switchIfEmpty(createNew(chatId));
    }

    /**
     * Обновить clientKey + state
     */
    public Mono<UserData> updateState(Long chatId, String clientKey, UserState newState) {
        return getOrCreate(chatId)
                .flatMap(user -> {
                    user.setClientKey(clientKey);
                    user.setState(newState);
                    return repo.save(user);
                });
    }

    /**
     * Найти по client_key (уникальному)
     */
    public Mono<UserData> getByClientKey(String clientKey) {
        return clientKey == null
                ? Mono.empty()
                : repo.findByClientKey(clientKey);
    }
}
