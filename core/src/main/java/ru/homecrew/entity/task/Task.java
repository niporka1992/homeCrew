package ru.homecrew.entity.task;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;
import ru.homecrew.entity.AppUser;
import ru.homecrew.entity.BaseEntity;
import ru.homecrew.enums.TaskStatus;
import ru.homecrew.enums.TaskTypeTrigger;

@Entity
@Table(name = "task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(
        callSuper = true,
        exclude = {"history"})
public class Task extends BaseEntity {

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TaskTypeTrigger type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private AppUser assignee;

    @OneToMany(mappedBy = "task")
    private List<TaskHistory> history;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
