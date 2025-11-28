package lk.tech.tgcontrollerbot.services;

import com.github.benmanes.caffeine.cache.Cache;
import lk.tech.tgcontrollerbot.model.UserData;
import lk.tech.tgcontrollerbot.model.UserState;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserDataCacheManager {

    private final Cache<Long, UserData> cache;

    public UserDataCacheManager(Cache<Long, UserData> cache) {
        this.cache = cache;
    }

    /**
     * Получить UserData по chatId, если нет — создать с дефолтами.
     */
    public UserData getOrCreate(Long chatId) {
        return cache.get(chatId, id -> UserData.builder().chatId(id).build());
    }

    /**
     * Просто получить, без создания.
     */
    public Optional<UserData> get(Long chatId) {
        return Optional.ofNullable(cache.getIfPresent(chatId));
    }

    /**
     * Обновить объект в кеше.
     */
    public void put(UserData userData) {
        cache.put(userData.getChatId(), userData);
    }

    /**
     * Обновить состояние пользователя.
     */
    public void updateState(Long chatId, String clientKey, UserState newState) {
        UserData data = getOrCreate(chatId);
        data.setState(newState);
        data.setClientKey(clientKey);
        cache.put(chatId, data);
    }

    /**
     * Удалить данные пользователя.
     */
    public void invalidate(Long chatId) {
        cache.invalidate(chatId);
    }

    /**
     * Полностью очистить кеш.
     */
    public void clearAll() {
        cache.invalidateAll();
    }

    /**
     * Получить статистику (hit/miss и др.)
     */
    public com.github.benmanes.caffeine.cache.stats.CacheStats stats() {
        return cache.stats();
    }

    /**
     * Найти UserData по clientKey
     */
    public Optional<UserData> getByClientKey(String clientKey) {
        if (clientKey == null) return Optional.empty();

        return cache.asMap()
                .values()
                .stream()
                .filter(u -> clientKey.equals(u.getClientKey()))
                .findFirst();
    }
}
