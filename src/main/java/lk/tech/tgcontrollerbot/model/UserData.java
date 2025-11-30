package lk.tech.tgcontrollerbot.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class UserData {

    @Id
    private Long id;  // SERIAL PRIMARY KEY

    @Column("chat_id")
    private Long chatId; // UNIQUE

    @Column("client_key")
    private String clientKey; // UNIQUE

    @Builder.Default
    @Column("state")
    private UserState state = UserState.IDLE;
}
