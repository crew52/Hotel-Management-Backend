package codegym.c10.hotel.mapper;

import codegym.c10.hotel.dto.ActivityLogDTO;
import codegym.c10.hotel.entity.ActivityLog;
import codegym.c10.hotel.entity.Employee;
import codegym.c10.hotel.entity.User;

public class ActivityLogMapper {
    public static ActivityLogDTO toDTO(ActivityLog log) {
        User user = log.getUser();
        String fullName = null;

        // Nếu dùng Spring Data, bạn có thể đảm bảo Employee được load qua User
        if (user != null && user.getId() != null) {
            Employee employee = user.getEmployee(); // Cần thêm mapping 2 chiều nếu chưa có
            fullName = (employee != null) ? employee.getFullName() : null;
        }

        return ActivityLogDTO.builder()
                .id(log.getId())
                .fullName(fullName)
                .username(user != null ? user.getUsername() : null)
                .action(log.getAction())
                .timestamp(log.getTimestamp())
                .description(log.getDescription())
                .build();
    }
}