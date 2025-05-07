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
     * Pointcut to intercept only methods annotated with @LogActivity
     */
    @Pointcut("@annotation(codegym.c10.hotel.annotation.LogActivity)")
    public void annotatedMethodsPointcut() {}

    /**
     * Advice to log activity after a method matching the pointcut successfully returns.
     *
     * @param joinPoint Provides access to the method execution details.
     * @param result The result returned by the intercepted method.
     */
    @AfterReturning(pointcut = "annotatedMethodsPointcut()", returning = "result")
    public void logActivity(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String methodName = method.getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            Object[] args = joinPoint.getArgs();

            LogActivity logAnnotation = null;
            
            // Check for @LogActivity annotation (implementation first, then interface)
            try {
                Method implMethod = joinPoint.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
                if (implMethod.isAnnotationPresent(LogActivity.class)) {
                    logAnnotation = implMethod.getAnnotation(LogActivity.class);
                }
            } catch (NoSuchMethodException e) {
                logger.trace("Không tìm thấy phương thức triển khai, kiểm tra interface: {}", e.getMessage());
            }
            
            if (logAnnotation == null && method.isAnnotationPresent(LogActivity.class)) {
                logAnnotation = method.getAnnotation(LogActivity.class);
            }

            if (logAnnotation == null) {
                logger.warn("Không tìm thấy annotation @LogActivity trên phương thức: {}", methodName);
                return;
            }

            // Get action and description from annotation
            String action = logAnnotation.action();
            String description = logAnnotation.description();
            
            // Generate default description if not provided
            if (description == null || description.trim().isEmpty()) {
                description = generateDefaultDescription(className, methodName, args);
            } else {
                String argsDesc = generateArgsDescription(args);
                if (!argsDesc.equals("()")) {
                    description = description + " | Args: " + argsDesc;
                }
            }

            // --- User identification logic ---
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
            
            if (logger.isDebugEnabled()) {
                logger.debug("🚀 Đã publish ActivityLogEvent: UserID='{}', Action='{}'",
                        userId != null ? userId : "SYSTEM", action);
            }

        } catch (Exception e) {
            logger.error("❌ Lỗi khi publish activity log event: {}.{}: {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    e.getMessage(), e);
        }
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
                    // For complex objects, just show class name
                    sj.add(arg.getClass().getSimpleName());
                }
            }
        }
        return sj.toString();
    }
}
