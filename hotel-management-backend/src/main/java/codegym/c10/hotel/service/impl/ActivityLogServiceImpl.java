package codegym.c10.hotel.service.impl;

import codegym.c10.hotel.dto.ActivityLogDTO;
import codegym.c10.hotel.mapper.ActivityLogMapper;
import codegym.c10.hotel.repository.IActivityLogRepository;
import codegym.c10.hotel.service.IActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements IActivityLogService {

    private final IActivityLogRepository activityLogRepository;

    @Override
    public Page<ActivityLogDTO> getAllActivityLogs(Pageable pageable) {
        return activityLogRepository.findByDeletedFalse(pageable)
                .map(ActivityLogMapper::toDTO);
    }

    @Override
    public Page<ActivityLogDTO> getLogsByUserId(Long userId, Pageable pageable) {
        return activityLogRepository.findByUserIdAndDeletedFalse(userId, pageable)
                .map(ActivityLogMapper::toDTO);
    }
}
