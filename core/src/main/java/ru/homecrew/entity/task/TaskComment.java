package ru.homecrew.entity.task;

import jakarta.persistence.*;
import lombok.*;
import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.BaseEntity;

@Entity
@Table(name = "task_comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class TaskComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "history_id", nullable = false)
    private TaskHistory history;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser author;

    @Column(name = "text", nullable = false, length = 2000)
    private String text;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
