package ru.homecrew.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_external_ids")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class UserExternalIds extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(name = "telegram_chat_id")
    private Long telegramChatId;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
