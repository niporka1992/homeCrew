package ru.homecrew.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.homecrew.dto.task.TaskCreateDto;
import ru.homecrew.dto.task.TaskDetailsDto;
import ru.homecrew.dto.task.TaskDto;
import ru.homecrew.service.task.TaskService;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_OWNER')")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(
            @RequestParam(required = false, name = "status") String status,
            @RequestParam(required = false, name = "id") Long userId) {
        return ResponseEntity.ok(taskService.getTasks(status, userId));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<TaskDetailsDto> getTaskDetails(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(taskService.getTaskDetails(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable(name = "id") Long id, @RequestBody TaskCreateDto dto) {
        return ResponseEntity.ok(taskService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable(name = "id") Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
