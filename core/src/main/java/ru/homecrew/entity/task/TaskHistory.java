package ru.homecrew.entity.task;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;
import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.BaseEntity;
import ru.homecrew.enums.TaskActionType;
import ru.homecrew.enums.TaskStatus;

@Entity
@Table(name = "task_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true, exclude = "attachments")
public class TaskHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser appUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private TaskActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_after", length = 30)
    private TaskStatus statusAfter;

    @OneToMany(mappedBy = "history", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskComment> comments;

    @OneToMany(mappedBy = "history", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskAttachment> attachments;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
