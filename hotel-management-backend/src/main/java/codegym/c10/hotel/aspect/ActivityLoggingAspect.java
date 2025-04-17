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
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Aspect để tự động ghi log các hoạt động.
 * Bắt các phương thức trong package service có tên save*, update*, delete*, remove*
 * hoặc các phương thức được đánh dấu bằng annotation @LogActivity.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class ActivityLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLoggingAspect.class);
    
    private final IActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    /**
     * Định nghĩa Pointcut:
     * - Bắt tất cả các phương thức trong các sub-package của codegym.c10.hotel.service
     * - VÀ tên phương thức bắt đầu bằng "save", "update", "delete", "remove"
     * - HOẶC phương thức có annotation @LogActivity
     * - HOẶC (tùy chọn) phương thức có annotation @Transactional và không phải là readOnly
     */
    @Pointcut("(execution(* codegym.c10.hotel.service..*.save*(..)) || " +
              "execution(* codegym.c10.hotel.service..*.create*(..)) || " +
              "execution(* codegym.c10.hotel.service..*.update*(..)) || " +
              "execution(* codegym.c10.hotel.service..*.delete*(..)) || " +
              "execution(* codegym.c10.hotel.service..*.remove*(..)) || " +
              "execution(* codegym.c10.hotel.service..*.add*(..)) || " +
              "execution(* codegym.c10.hotel.service..*.change*(..)) || " +
              "@annotation(codegym.c10.hotel.annotation.LogActivity)) && " +
              "!execution(* codegym.c10.hotel.service.user.UserService.loadUserByUsername(..))")
    public void serviceModificationOrAnnotated() {}

    /**
     * Advice chạy sau khi phương thức khớp với pointcut 'serviceModificationOrAnnotated' thực thi thành công.
     * Tự động lấy thông tin người dùng hiện tại từ SecurityContext và ghi log hoạt động.
     *
     * @param joinPoint Điểm cắt (phương thức được gọi)
     */
    @AfterReturning(pointcut = "serviceModificationOrAnnotated()")
    public void logActivity(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogActivity logActivityAnnotation = method.getAnnotation(LogActivity.class);

            String action;
            String description;
            String methodName = method.getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();

            if (logActivityAnnotation != null) {
                action = logActivityAnnotation.action();
                description = logActivityAnnotation.description();
                if (description.isEmpty()) {
                    description = "Đã thực hiện: " + action + " thông qua phương thức " + className + "." + methodName;
                }
            } else {
                action = inferActionFromMethodName(methodName);
                description = generateDefaultDescription(className, methodName, joinPoint.getArgs());
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = "SYSTEM";
            Long userIdValue = null;

            if (authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"))) {

                Object principal = authentication.getPrincipal();
                if (principal instanceof UserPrinciple) {
                    UserPrinciple userPrinciple = (UserPrinciple) principal;
                    userIdValue = userPrinciple.getId();
                    username = userPrinciple.getUsername();
                } else if (principal instanceof String) {
                    username = (String) principal;
                    User userByUsername = userRepository.findByUsername(username);
                    if (userByUsername != null) {
                        userIdValue = userByUsername.getId();
                    } else {
                        logger.warn("Không tìm thấy user trong DB với username từ Principal: {}", username);
                    }
                } else {
                    logger.warn("Principal không phải là UserPrinciple hoặc String: {}", principal.getClass().getName());
                }
            } else {
                logger.info("Không có người dùng nào được xác thực, hành động được thực hiện bởi SYSTEM hoặc anonymous.");
            }

            final Long userId = userIdValue;

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
                logger.info("ACTIVITY LOGGED: User='{}'(ID={}), Action='{}', Description='{}'",
                    username, userId, action, description);
            } else {
                ActivityLog systemLog = ActivityLog.builder()
                        .user(null)
                        .action(action)
                        .timestamp(LocalDateTime.now())
                        .description("[SYSTEM] " + description)
                        .build();
                activityLogRepository.save(systemLog);
                logger.info("ACTIVITY LOGGED: User='{}', Action='{}', Description='{}'",
                        username, action, "[SYSTEM] " + description);
            }

        } catch (Exception e) {
            logger.error("Lỗi khi ghi log hoạt động cho phương thức {}.{}: {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    e.getMessage(), e);
        }
    }

    private String inferActionFromMethodName(String methodName) {
        String lowerMethodName = methodName.toLowerCase();
        if (lowerMethodName.startsWith("save") || lowerMethodName.startsWith("create") || lowerMethodName.startsWith("add") || lowerMethodName.startsWith("register")) {
            return "CREATE";
        } else if (lowerMethodName.startsWith("update") || lowerMethodName.startsWith("modify") || lowerMethodName.startsWith("change") || lowerMethodName.startsWith("edit")) {
            return "UPDATE";
        } else if (lowerMethodName.startsWith("delete") || lowerMethodName.startsWith("remove")) {
            return "DELETE";
        } else if (lowerMethodName.startsWith("find") || lowerMethodName.startsWith("get") || lowerMethodName.startsWith("load") || lowerMethodName.startsWith("search")) {
            return "READ";
        }
        return methodName.toUpperCase();
    }

    private String generateDefaultDescription(String className, String methodName, Object[] args) {
        StringJoiner sj = new StringJoiner(", ", "(", ")");
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof String || arg instanceof Number || arg instanceof Boolean || arg == null) {
                    sj.add(String.valueOf(arg));
                } else {
                    String argDesc = arg.getClass().getSimpleName();
                    try {
                        Method getIdMethod = arg.getClass().getMethod("getId");
                        Object id = getIdMethod.invoke(arg);
                        argDesc += "[id=" + id + "]";
                    } catch (NoSuchMethodException e) {
                    } catch (Exception e) {
                        logger.trace("Error getting id for arg: {}", arg.getClass().getName(), e);
                    }
                    sj.add(argDesc);
                }
            }
        }
        return String.format("Executed method: %s.%s%s", className, methodName, sj.toString());
    }
} 