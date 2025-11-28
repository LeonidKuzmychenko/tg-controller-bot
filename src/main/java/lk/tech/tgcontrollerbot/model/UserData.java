package lk.tech.tgcontrollerbot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserData {

    private Long chatId;
    private String clientKey;

    @Builder.Default
    private UserState state = UserState.IDLE;
}
