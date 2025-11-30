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
     * Найти пользователя по chat_id (уникальному полю)
     */
    public Mono<UserData> getByChatId(Long chatId) {
        return repo.findByChatId(chatId);
    }

    /**
     * Создать нового пользователя, если записи нет
     */
    public Mono<UserData> create(Long chatId) {
        UserData newUser = UserData.builder()
                .chatId(chatId)
                .state(UserState.IDLE)
                .build();

        return repo.save(newUser);
    }

    /**
     * Получить пользователя или создать, если отсутствует
     */
    public Mono<UserData> getOrCreate(Long chatId) {
        return getByChatId(chatId)
                .switchIfEmpty(create(chatId));
    }

    /**
     * Обновить clientKey и state пользователя
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
        if (clientKey == null) {
            return Mono.empty();
        }
        return repo.findByClientKey(clientKey);
    }
}
