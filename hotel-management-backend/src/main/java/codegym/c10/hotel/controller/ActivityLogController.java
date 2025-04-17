package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.ActivityLogDTO;
import codegym.c10.hotel.service.IActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/activity-logs", "/api/logs"})
@RequiredArgsConstructor
public class ActivityLogController {

    private final IActivityLogService activityLogService;


    @GetMapping()
    public ResponseEntity<Page<ActivityLogDTO>> getAllActivityLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ActivityLogDTO> logs = activityLogService.getAllActivityLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{userId}/userId")
    public ResponseEntity<Page<ActivityLogDTO>> getLogsByUserIdWithPath(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<ActivityLogDTO> logs = activityLogService.getLogsByUserId(userId, pageable);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<Page<ActivityLogDTO>> getLogsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<ActivityLogDTO> logs = activityLogService.getLogsByUserId(userId, pageable);
        return ResponseEntity.ok(logs);
    }
}