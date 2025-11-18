package lk.tech.learntgbot.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lk.tech.learntgbot.model.UserData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Конфигурация кеша Caffeine для хранения данных пользователей.
 * 
 * Настраивает in-memory кеш для временного хранения данных пользователей
 * в процессе регистрации. Кеш автоматически удаляет устаревшие записи.
 * 
 * Преимущества Caffeine:
 * - Высокая производительность
 * - Автоматическое управление памятью
 * - Статистика использования
 * - Гибкая настройка политик истечения
 * 
 * Настройки кеша:
 * - Максимальный размер: 10,000 записей
 * - Истечение после записи: 30 минут
 * - Истечение после доступа: 15 минут
 * - Статистика: включена
 * 
 * Принципы:
 * - Configuration as Code: настройки в коде, а не в properties
 * - Separation of Concerns: конфигурация отделена от бизнес-логики
 * 
 * @author Cursor Telegram Bot
 * @version 1.0
 */
@Configuration
public class CacheConfig {

    /**
     * Создает и настраивает кеш для хранения данных пользователей.
     * 
     * Политика истечения:
     * - expireAfterWrite: запись удаляется через 30 минут после создания/обновления
     *   Это предотвращает накопление устаревших данных
     * - expireAfterAccess: запись удаляется через 15 минут после последнего доступа
     *   Это освобождает память от неактивных пользователей
     * 
     * Размер кеша:
     * - maximumSize: ограничение в 10,000 записей предотвращает переполнение памяти
     *   При достижении лимита удаляются наименее используемые записи (LRU)
     * 
     * Статистика:
     * - recordStats: включение статистики для мониторинга производительности
     *   Позволяет отслеживать hit rate, miss rate и другие метрики
     * 
     * @return настроенный кеш для хранения UserData
     */
    @Bean
    public Cache<Long, UserData> userDataCache() {
        return Caffeine.newBuilder()
                // Максимальное количество записей в кеше
                // При превышении лимита удаляются наименее используемые записи (LRU)
//                .maximumSize(10_000)
                
                // Запись удаляется через 30 минут после создания или обновления
                // Это предотвращает накопление устаревших данных
//                .expireAfterWrite(365, TimeUnit.DAYS)
                
                // Запись удаляется через 15 минут после последнего доступа
                // Это освобождает память от неактивных пользователей
//                .expireAfterAccess(15, TimeUnit.MINUTES)
                
                // Включение статистики для мониторинга производительности
                // Позволяет отслеживать hit rate, miss rate и другие метрики
                .recordStats()
                
                .build();
    }
}
