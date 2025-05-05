package codegym.c10.hotel.audit;

import codegym.c10.hotel.dto.auth.UserPrinciple;
import org.hibernate.envers.RevisionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Listener that captures the current user during database revisions
 * for audit tracking purposes
 */
public class AuditRevisionListener implements RevisionListener {

    private static final Logger logger = LoggerFactory.getLogger(AuditRevisionListener.class);

    @Override
    public void newRevision(Object revisionEntity) {
        AuditRevisionEntity auditEntity = (AuditRevisionEntity) revisionEntity;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String currentUsername = "SYSTEM";
        Long currentUserId = null;

        if (authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String && 
                authentication.getPrincipal().equals("anonymousUser"))) {

            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrinciple) {
                UserPrinciple userPrinciple = (UserPrinciple) principal;
                currentUserId = userPrinciple.getId();
                currentUsername = userPrinciple.getUsername();
            } else if (principal instanceof String) {
                currentUsername = (String) principal;
            }
            
            logger.debug("Audit Revision - User: {} (ID: {})", currentUsername, currentUserId);
        } else {
            logger.debug("Audit Revision - User: SYSTEM (No authentication)");
        }

        auditEntity.setUserId(currentUserId);
        auditEntity.setUsername(currentUsername);
    }
} 