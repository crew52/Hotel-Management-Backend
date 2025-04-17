package codegym.c10.hotel.aspect;

import codegym.c10.hotel.dto.auth.UserPrinciple;
import codegym.c10.hotel.entity.ActivityLog;
import codegym.c10.hotel.entity.User;
import codegym.c10.hotel.repository.IActivityLogRepository;
import codegym.c10.hotel.repository.UserRepository;
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

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.StringJoiner;

@Aspect
@Component
public class ActivityLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLoggingAspect.class);

    private final IActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public ActivityLoggingAspect(IActivityLogRepository activityLogRepository, UserRepository userRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        logger.info("===== ACTIVITY LOGGING ASPECT INITIALIZED =====");
    }

    /**
     * Chỉ theo dõi các phương thức có tên liên quan đến hành vi ghi dữ liệu (CUD).
     */
    @Pointcut("" +
            "execution(* codegym.c10.hotel.service..*.*save*(..)) || " +
            "execution(* codegym.c10.hotel.service..*.*create*(..)) || " +
            "execution(* codegym.c10.hotel.service..*.*update*(..)) || " +
            "execution(* codegym.c10.hotel.service..*.*delete*(..)) || " +
            "execution(* codegym.c10.hotel.service..*.*remove*(..)) || " +
            "execution(* codegym.c10.hotel.service..*.*add*(..)) || " +
            "execution(* codegym.c10.hotel.service..*.*change*(..))")
    public void modifyingMethodsOnly() {}

    @AfterReturning(pointcut = "modifyingMethodsOnly()")
    public void logActivity(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String methodName = method.getName().toLowerCase();

            // Bước kiểm tra để loại bỏ chắc chắn method đọc dữ liệu
            if (methodName.startsWith("find") ||
                    methodName.startsWith("get") ||
                    methodName.startsWith("load") ||
                    methodName.startsWith("search") ||
                    methodName.startsWith("list") ||
                    methodName.startsWith("is") ||
                    methodName.startsWith("exists") ||
                    methodName.startsWith("count")) {
                logger.debug("⛔ BỎ QUA log vì đây là phương thức đọc: {}", method.getName());
                return;
            }

            logger.info("===== ASPECT INTERCEPTED METHOD: {}.{} =====",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName());

            String action = inferActionFromMethodName(method.getName());
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String description = generateDefaultDescription(className, method.getName(), joinPoint.getArgs());

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
                    }
                }
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
                logger.info("✅ ACTIVITY LOGGED: User='{}'(ID={}), Action='{}', Description='{}'",
                        username, userId, action, description);
            } else {
                ActivityLog systemLog = ActivityLog.builder()
                        .user(null)
                        .action(action)
                        .timestamp(LocalDateTime.now())
                        .description("[SYSTEM] " + description)
                        .build();
                activityLogRepository.save(systemLog);
                logger.info("✅ ACTIVITY LOGGED: User='{}', Action='{}', Description='{}'",
                        username, action, "[SYSTEM] " + description);
            }

        } catch (Exception e) {
            logger.error("❌ Lỗi khi ghi log hoạt động: {}.{}: {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    e.getMessage(), e);
        }
    }

    private String inferActionFromMethodName(String methodName) {
        String lowerMethodName = methodName.toLowerCase();
        if (lowerMethodName.contains("save") || lowerMethodName.contains("create") ||
                lowerMethodName.contains("add") || lowerMethodName.contains("register") || lowerMethodName.contains("insert")) {
            return "CREATE";
        } else if (lowerMethodName.contains("update") || lowerMethodName.contains("modify") ||
                lowerMethodName.contains("change") || lowerMethodName.contains("edit")) {
            return "UPDATE";
        } else if (lowerMethodName.contains("delete") || lowerMethodName.contains("remove")) {
            return "DELETE";
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
                    } catch (Exception ignored) {
                    }
                    sj.add(argDesc);
                }
            }
        }
        return String.format("Executed method: %s.%s%s", className, methodName, sj.toString());
    }
}
