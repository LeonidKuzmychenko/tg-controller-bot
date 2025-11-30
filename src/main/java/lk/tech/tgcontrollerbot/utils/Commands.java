package lk.tech.tgcontrollerbot.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum Commands {
    SHUTDOWN("/shutdown","Выключение пк"),
    SCREENSHOT("/screenshot","Скриншот экрана"),
//    INFO("/info","Комплектующие пк"),
    IP("/ip","Предоставление IP");
//    LOAD("/load","Нагрузка ПК"),
//    PROCESSES("/processes","Топ 10 процессов"),
//    SPEEDTEST("/speedtest","Тест скорости интернета"),
//    TEMP("/temp","Температуры");

    private final String command;
    private final String description;

    public static Map<String, String> map() {
        return Arrays.stream(Commands.values())
                .collect(Collectors.toMap(
                        Commands::getCommand,
                        Commands::getDescription
                ));
    }

    public static String getDescription(String command) {
        for (Commands commands : Commands.values()) {
            if (commands.getCommand().equals(command)) {
                return commands.getDescription();
            }
        }
        return null;
    }

    public static boolean isExist(String command) {
        if (!StringUtils.hasLength(command)) {
            return false;
        }
        for (Commands commands : Commands.values()) {
            if (commands.getCommand().equals(command)) {
                return true;
            }
        }
        return false;
    }
}
