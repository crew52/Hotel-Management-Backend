package codegym.c10.hotel.service;

import codegym.c10.hotel.dto.ActivityLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IActivityLogService {
    Page<ActivityLogDTO> getAllActivityLogs(Pageable pageable);
}

