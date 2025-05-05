package codegym.c10.hotel.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;

/**
 * Event representing an activity that should be logged.
 * Used for asynchronous logging to improve performance.
 */
@Getter
public class ActivityLogEvent extends ApplicationEvent {

    private final Long userId;
    private final String action;
    private final String description;
    private final LocalDateTime eventTimestamp;

    /**
     * Constructor for the activity log event.
     * @param source The object on which the event initially occurred.
     * @param userId ID of the user performing the action (null if system).
     * @param action The action performed.
     * @param description Description of the action.
     * @param eventTimestamp Time the action occurred.
     */
    public ActivityLogEvent(Object source, Long userId, String action, String description, LocalDateTime eventTimestamp) {
        super(source);
        this.userId = userId;
        this.action = action;
        this.description = description;
        this.eventTimestamp = eventTimestamp;
    }
} 