package codegym.c10.hotel.aspect;

import codegym.c10.hotel.annotation.LogActivity;
import codegym.c10.hotel.dto.auth.UserPrinciple;
import codegym.c10.hotel.entity.ActivityLog;
import codegym.c10.hotel.entity.User;
import codegym.c10.hotel.repository.IActivityLogRepository;
import codegym.c10.hotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Aspect để tự động ghi log các hoạt động được đánh dấu bằng annotation @LogActivity.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class ActivityLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLoggingAspect.class);
    
    private final IActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    /**
     * Advice chạy sau khi phương thức có annotation @LogActivity thực thi thành công.
     * Tự động lấy thông tin người dùng hiện tại từ SecurityContext và ghi log hoạt động.
     *
     * @param joinPoint Điểm cắt (phương thức được gọi)
     */
    @AfterReturning(pointcut = "@annotation(codegym.c10.hotel.annotation.LogActivity)")
    public void logActivity(JoinPoint joinPoint) {
        try {
            // 1. Lấy thông tin annotation từ phương thức
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogActivity logActivityAnnotation = method.getAnnotation(LogActivity.class);

            String action = logActivityAnnotation.action();
            String description = logActivityAnnotation.description();
            if (description.isEmpty()) {
                // Tạo mô tả mặc định nếu không được cung cấp
                description = "Đã thực hiện thao tác: " + action;
            }

            // 2. Lấy thông tin người dùng từ SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            final String[] username = {"SYSTEM"}; // Mặc định nếu không xác định được người dùng
            Long userIdValue = null;

            if (authentication != null && authentication.isAuthenticated() && 
                !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"))) {
                
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserPrinciple) {
                    UserPrinciple userPrinciple = (UserPrinciple) principal;
                    userIdValue = userPrinciple.getId();
                    username[0] = userPrinciple.getUsername();
                } else if (principal instanceof String) {
                    // Nếu principal là String, đó có thể là username
                    username[0] = (String) principal;
                    User user = userRepository.findByUsername(username[0]);
                    if (user != null) {
                        userIdValue = user.getId();
                    }
                }
            }

            // 3. Tạo và lưu log
            final Long userId = userIdValue; // Tạo biến final để sử dụng trong lambda
            if (userId != null) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

                ActivityLog log = ActivityLog.builder()
                        .user(user)
                        .action(action)
                        .timestamp(LocalDateTime.now())
                        .description(description)
                        .build();
                
                activityLogRepository.save(log);
                logger.info("Đã ghi log hoạt động: Người dùng='{}', Hành động='{}', Mô tả='{}'", 
                    username[0], action, description);
            } else {
                logger.warn("Không thể ghi log hoạt động vì không xác định được userId. Hành động: {}", action);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi ghi log hoạt động: {}", e.getMessage(), e);
        }
    }
} 