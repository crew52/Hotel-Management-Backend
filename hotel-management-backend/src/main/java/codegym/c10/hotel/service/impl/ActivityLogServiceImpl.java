package codegym.c10.hotel.service.impl;

import codegym.c10.hotel.dto.ActivityLogDTO;
import codegym.c10.hotel.mapper.ActivityLogMapper;
import codegym.c10.hotel.repository.IActivityLogRepository;
import codegym.c10.hotel.service.IActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements IActivityLogService {

    private final IActivityLogRepository activityLogRepository;

    @Override
    public List<ActivityLogDTO> getAllActivityLogs() {
        return activityLogRepository.findByDeletedFalse()
                .stream()
                .map(ActivityLogMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ActivityLogDTO> getLogsByUserId(Long userId) {
        return activityLogRepository.findByUserIdAndDeletedFalse(userId)
                .stream()
                .map(ActivityLogMapper::toDTO)
                .collect(Collectors.toList());
    }
}
