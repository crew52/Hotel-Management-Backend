package codegym.c10.hotel.aspect;

import codegym.c10.hotel.annotation.LogActivity;
import codegym.c10.hotel.dto.auth.UserPrinciple;
import codegym.c10.hotel.entity.User;
import codegym.c10.hotel.event.ActivityLogEvent;
import codegym.c10.hotel.repository.UserRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ActivityLoggingAspect(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        logger.info("===== ACTIVITY LOGGING ASPECT INITIALIZED (Async Mode) =====");
    }

    /**
     * Pointcut to intercept ALL methods within the service packages and subpackages.
     * Filtering logic within the advice will determine if a log is actually generated.
     */
    @Pointcut("execution(* codegym.c10.hotel.service..*.*(..)) && " +
              "!execution(* codegym.c10.hotel.service.auth.*.*(..)) && " + // Example: Exclude auth service if needed
              "!execution(* codegym.c10.hotel.service.jwt.*.*(..))"      // Example: Exclude jwt service
    )
    public void allServiceMethodsPointcut() {}

    /**
     * Advice to log activity after a method matching the pointcut successfully returns.
     * It prioritizes @LogActivity if present, otherwise infers details, and filters out most read methods.
     *
     * @param joinPoint Provides access to the method execution details.
     * @param result The result returned by the intercepted method.
     */
    @AfterReturning(pointcut = "allServiceMethodsPointcut()", returning = "result")
    public void logActivity(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String methodName = method.getName();
            String lowerMethodName = methodName.toLowerCase();

            // Optional: Refined check to skip purely read methods if pointcut is too broad
             if (lowerMethodName.startsWith("find") ||
                 lowerMethodName.startsWith("get") ||
                 lowerMethodName.startsWith("load") ||
                 lowerMethodName.startsWith("search") ||
                 lowerMethodName.startsWith("list") ||
                 lowerMethodName.startsWith("is") ||
                 lowerMethodName.startsWith("exists") ||
                 lowerMethodName.startsWith("count")) {
                  // Check if explicitly annotated for logging despite being a read method
                 boolean isAnnotated = false;
                 try {
                     Method implMethod = joinPoint.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
                     if (implMethod.isAnnotationPresent(LogActivity.class)) {
                         isAnnotated = true;
                     }
                 } catch (NoSuchMethodException ignored) {}
                 if (!isAnnotated && !method.isAnnotationPresent(LogActivity.class)) {
                     logger.trace("⛔ Bỏ qua log (phương thức đọc không có @LogActivity): {}", method.getName());
                     return;
                 }
             }

            logger.info("===== ASPECT INTERCEPTED METHOD (Async): {}.{} =====",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    methodName);

            String action;
            String description;
            String className = joinPoint.getTarget().getClass().getSimpleName();
            Object[] args = joinPoint.getArgs();

            // Check for @LogActivity annotation (implementation first, then interface)
            LogActivity logAnnotation = null;
            try {
                Method implMethod = joinPoint.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
                if (implMethod.isAnnotationPresent(LogActivity.class)) {
                    logAnnotation = implMethod.getAnnotation(LogActivity.class);
                    logger.trace("Annotation found on implementation method: {}", methodName);
                }
            } catch (NoSuchMethodException e) {
                logger.trace("Không tìm thấy phương thức triển khai, kiểm tra interface: {}", e.getMessage());
            }
            if (logAnnotation == null && method.isAnnotationPresent(LogActivity.class)) {
                logAnnotation = method.getAnnotation(LogActivity.class);
                 logger.trace("Annotation found on interface method: {}", methodName);
            }

            // Determine action and description
            if (logAnnotation != null) {
                // Use annotation details
                action = logAnnotation.action();
                description = logAnnotation.description();
                if (description == null || description.trim().isEmpty()) {
                    description = generateDefaultDescription(className, methodName, args);
                    logger.debug("Sử dụng @LogActivity (mô tả trống): action='{}', description='{}'", action, description);
                } else {
                    String argsDesc = generateArgsDescription(args);
                    if (!argsDesc.equals("()")) {
                        description = description + " | Args: " + argsDesc;
                    }
                    logger.debug("Sử dụng @LogActivity: action='{}', description='{}'", action, description);
                }
            } else {
                // Infer details if no annotation
                action = inferActionFromMethodName(lowerMethodName); // Use lower case for inference
                description = generateDefaultDescription(className, methodName, args);
                logger.debug("Sử dụng logic suy luận (không có @LogActivity): action='{}', description='{}'", action, description);
            }

            // --- User identification logic (remains the same) ---
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
            // --- End User identification logic ---

            final Long userId = userIdValue;

            // Create and publish event
            LocalDateTime timestamp = LocalDateTime.now();
            ActivityLogEvent logEvent = new ActivityLogEvent(
                    this,
                    userId,
                    action,
                    description,
                    timestamp
            );

            eventPublisher.publishEvent(logEvent);
            logger.info("🚀 Đã publish ActivityLogEvent: UserID='{}', Action='{}'",
                    userId != null ? userId : "SYSTEM", action);

        } catch (Exception e) {
            logger.error("❌ Lỗi khi publish activity log event: {}.{}: {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    e.getMessage(), e);
        }
    }

    /**
     * Infers a more descriptive action name (often in Vietnamese or standard codes)
     * from the method name if no @LogActivity is present.
     * @param lowerMethodName The method name in lower case.
     * @return An inferred action name (e.g., TAO_MOI, CAP_NHAT, XOA) or a default.
     */
    private String inferActionFromMethodName(String lowerMethodName) {
        // Prioritize common CUD operations
        if (lowerMethodName.contains("save") || lowerMethodName.contains("create") ||
            lowerMethodName.contains("add") || lowerMethodName.contains("register") || lowerMethodName.contains("insert")) {
            return "TAO_MOI"; // CREATE
        } else if (lowerMethodName.contains("update") || lowerMethodName.contains("modify") ||
                   lowerMethodName.contains("change") || lowerMethodName.contains("edit")) {
            return "CAP_NHAT"; // UPDATE
        } else if (lowerMethodName.contains("delete") || lowerMethodName.contains("remove")) {
            return "XOA"; // DELETE
        }

        // Specific business actions
        else if (lowerMethodName.contains("login")) {
            return "DANG_NHAP"; // LOGIN
        } else if (lowerMethodName.contains("logout")) {
            return "DANG_XUAT"; // LOGOUT
        } else if (lowerMethodName.contains("checkin") || lowerMethodName.contains("check_in")) {
             return "NHAN_PHONG"; // CHECK_IN
        } else if (lowerMethodName.contains("checkout") || lowerMethodName.contains("check_out")) {
             return "TRA_PHONG"; // CHECK_OUT
        } else if (lowerMethodName.contains("changepassword")) {
             return "DOI_MAT_KHAU"; // CHANGE_PASSWORD
        } else if (lowerMethodName.contains("assign") || lowerMethodName.contains("grant")) {
             return "GAN_QUYEN"; // ASSIGN_ROLE/PERMISSION
        } else if (lowerMethodName.contains("revoke") || lowerMethodName.contains("remove") && (lowerMethodName.contains("role") || lowerMethodName.contains("permission"))){
            return "THU_HOI_QUYEN"; // REVOKE_ROLE/PERMISSION
        }

        // Read operations (if they somehow pass the filter or are annotated)
         else if (lowerMethodName.startsWith("find") || lowerMethodName.startsWith("get") ||
                  lowerMethodName.startsWith("search") || lowerMethodName.startsWith("list") ||
                   lowerMethodName.startsWith("view")) {
             return "XEM"; // VIEW / SEARCH
         }

        // Default action if no specific pattern matches
        String inferred = lowerMethodName.replaceAll("([A-Z])", "_$1").toUpperCase();
        inferred = inferred.replaceAll("[^A-Z0-9_]", "").substring(0, Math.min(inferred.length(), 50));
        return inferred.isEmpty() ? "HANH_DONG_KHAC" : inferred; // OTHER_ACTION
    }

    /**
     * Generates a more user-friendly default description string.
     */
    private String generateDefaultDescription(String className, String methodName, Object[] args) {
        // Attempt to make class/method name more readable
        String readableMethodName = methodName.replaceAll("([A-Z])", " $1").trim(); // Add space before caps
        // Remove common prefixes/suffixes if needed for readability
        String serviceName = className.replace("ServiceImpl", "").replace("Service", "");

        String argsDesc = generateArgsDescription(args);
        // Use Vietnamese for the default description format
        return String.format("Đã thực thi: %s tại %s %s", readableMethodName, serviceName, argsDesc);
    }
    
    private String generateArgsDescription(Object[] args) {
        StringJoiner sj = new StringJoiner(", ", "(", ")");
        if (args != null) {
            for (Object arg : args) {
                if (arg == null) {
                    sj.add("null");
                } else if (arg instanceof String || arg instanceof Number || arg instanceof Boolean || arg instanceof Enum<?>) {
                    sj.add(String.valueOf(arg));
                } else {
                    String argDesc = arg.getClass().getSimpleName();
                    try {
                        Method getIdMethod = arg.getClass().getMethod("getId");
                        Object id = getIdMethod.invoke(arg);
                        argDesc += "[id=" + id + "]";
                    } catch (Exception ignored) {
                        // No getId method or exception during invocation
                    }
                    sj.add(argDesc);
                }
            }
        }
        return sj.toString();
    }
}
