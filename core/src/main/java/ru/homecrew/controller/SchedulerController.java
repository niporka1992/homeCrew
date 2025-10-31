package ru.homecrew.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.homecrew.dto.scheduler.jobs.JobInfoDto;
import ru.homecrew.dto.scheduler.type.ScheduleDto;
import ru.homecrew.dto.scheduler.type.SimpleScheduleDto;
import ru.homecrew.service.scheduler.SchedulerService;

@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class SchedulerController {

    private final SchedulerService schedulerService;

    @PostMapping("/job/cron")
    public ResponseEntity<String> scheduleCron(@RequestBody ScheduleDto dto) {
        schedulerService.scheduleCronJob(dto);
        return ResponseEntity.ok("✅ CRON-задача добавлена: " + dto.jobName());
    }

    @PostMapping("/job/simple")
    public ResponseEntity<String> scheduleSimple(@RequestBody SimpleScheduleDto dto) {
        schedulerService.scheduleSimpleJob(dto);
        return ResponseEntity.ok("✅ SIMPLE-задача добавлена: " + dto.jobName());
    }

    @PatchMapping("/job/{jobName}/status")
    public ResponseEntity<String> changeJobStatus(
            @PathVariable("jobName") String jobName, @RequestParam("active") boolean active) {
        schedulerService.setJobActive(jobName, active);
        return ResponseEntity.ok(active ? "▶️ Задача возобновлена: " + jobName : "⏸ Задача приостановлена: " + jobName);
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobInfoDto>> getJobs(@RequestParam("status") String status) {
        return ResponseEntity.ok(schedulerService.getAllJobsByStatus(status));
    }
}
