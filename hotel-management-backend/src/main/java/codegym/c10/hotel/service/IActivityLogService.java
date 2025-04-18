package codegym.c10.hotel.service;

import codegym.c10.hotel.dto.ActivityLogDTO;

import java.util.List;

public interface IActivityLogService {
    List<ActivityLogDTO> getAllActivityLogs();
    List<ActivityLogDTO> getLogsByUserId(Long userId);
}

