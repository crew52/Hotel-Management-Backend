package codegym.c10.hotel.listener;

import codegym.c10.hotel.entity.ActivityLog;
import codegym.c10.hotel.entity.User;
import codegym.c10.hotel.event.ActivityLogEvent;
import codegym.c10.hotel.repository.IActivityLogRepository;
import codegym.c10.hotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listener that handles activity logging events asynchronously
 */
@Component
@RequiredArgsConstructor
public class ActivityLogEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLogEventListener.class);

    private final IActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    /**
     * Handles activity log events asynchronously
     */
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleActivityLogEvent(ActivityLogEvent event) {
        logger.debug("⚡️ Received async event to log activity: action='{}'", event.getAction());
        try {
            User user = null;
            String logDescription = event.getDescription();

            if (event.getUserId() != null) {
                user = userRepository.findById(event.getUserId()).orElse(null);
                if (user == null) {
                    logger.warn("⚠️ User with ID {} not found while handling async event.", event.getUserId());
                    logDescription = String.format("[User ID %d Not Found] %s", event.getUserId(), event.getDescription());
                }
            } else {
                logDescription = "[SYSTEM] " + event.getDescription();
            }

            ActivityLog log = ActivityLog.builder()
                    .user(user)
                    .action(event.getAction())
                    .timestamp(event.getEventTimestamp())
                    .description(logDescription)
                    .build();

            activityLogRepository.save(log);
            logger.info("✅ ASYNC LOG SAVED: UserID='{}', Action='{}'",
                    (user != null ? user.getId() : "SYSTEM"), event.getAction());

        } catch (Exception e) {
            logger.error("❌ Error saving activity log asynchronously: {}", e.getMessage(), e);
        }
    }
} 