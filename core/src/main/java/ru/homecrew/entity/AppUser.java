package ru.homecrew.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;
import ru.homecrew.entity.task.Task;
import ru.homecrew.entity.task.TaskHistory;
import ru.homecrew.enums.Role;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(
        callSuper = true,
        exclude = {"tasks", "taskHistories"})
public class AppUser extends BaseEntity {

    @Column(name = "username", nullable = false, length = 100, unique = true)
    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "email", length = 200)
    private String email;

    @OneToMany(mappedBy = "assignee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = false;

    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskHistory> taskHistories;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
