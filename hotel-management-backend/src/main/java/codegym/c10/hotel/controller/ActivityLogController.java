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

import java.util.List;

@RestController
@RequestMapping({"/api/activity-logs", "/api/logs"})
@RequiredArgsConstructor
public class ActivityLogController {

    private final IActivityLogService activityLogService;


    @GetMapping()
    public ResponseEntity<List<ActivityLogDTO>> getAllActivityLog() {
        List<ActivityLogDTO> logs = activityLogService.getAllActivityLogs();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{userId}/userId")
    public ResponseEntity<List<ActivityLogDTO>> getLogsByUserIdWithPath(@PathVariable Long userId) {
        List<ActivityLogDTO> logs = activityLogService.getLogsByUserId(userId);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<List<ActivityLogDTO>> getLogsByUserId(@PathVariable Long userId) {
        List<ActivityLogDTO> logs = activityLogService.getLogsByUserId(userId);
        return ResponseEntity.ok(logs);
    }
}