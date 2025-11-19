package lk.tech.learntgbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Модель данных пользователя.
 * <p>
 * Хранит информацию о пользователе, собранную в процессе регистрации:
 * - Имя пользователя
 * - Возраст пользователя
 * - Пол пользователя
 * - Текущее состояние диалога
 * <p>
 * Используется для:
 * - Хранения данных в кеше Caffeine
 * - Передачи данных между сервисами
 * - Отображения информации пользователю
 * <p>
 * Принципы:
 * - Immutability: можно создать через Builder
 * - Value Object: представляет набор связанных данных
 *
 * @author Cursor Telegram Bot
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserData {

    /**
     * Имя пользователя.
     * <p>
     * Валидируется на длину (максимум 100 символов) и непустоту.
     * Может содержать любые символы, кроме управляющих.
     */
    private String name;

    /**
     * Возраст пользователя.
     * <p>
     * Валидируется на диапазон от 1 до 150 лет.
     * Должен быть положительным целым числом.
     */
    private Integer age;

    /**
     * Пол пользователя.
     * <p>
     * Нормализуется к значениям "Мужской" или "Женский".
     * Поддерживает ввод на русском и английском языках.
     */
    private String gender;

    /**
     * Текущее состояние диалога с пользователем.
     * <p>
     * Определяет, на каком этапе регистрации находится пользователь:
     * - IDLE: ожидание команды /start
     * - ASKING_NAME: запрос имени
     * - ASKING_AGE: запрос возраста
     * - ASKING_GENDER: запрос пола
     * - COMPLETED: регистрация завершена
     * <p>
     * Значение по умолчанию: IDLE
     */
    @Builder.Default
    private UserState state = UserState.IDLE;
}
