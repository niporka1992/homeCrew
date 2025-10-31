package ru.homecrew.entity.task;

import jakarta.persistence.*;
import lombok.*;
import ru.homecrew.entity.BaseEntity;

@Entity
@Table(name = "task_attachment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class TaskAttachment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "history_id", nullable = false)
    private TaskHistory history;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
