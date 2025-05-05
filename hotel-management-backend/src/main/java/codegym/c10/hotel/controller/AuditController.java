package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.EntityRevisionDTO;
import codegym.c10.hotel.entity.Booking;
import codegym.c10.hotel.entity.Room;
import codegym.c10.hotel.entity.RoomCategory;
import codegym.c10.hotel.entity.User;
import codegym.c10.hotel.service.audit.IAuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller cung cấp API truy xuất lịch sử thay đổi entity
 */
@RestController
@RequestMapping("/api/v1/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final IAuditService auditService;
    private final Map<String, Class<?>> entityClassMap;

    public AuditController(IAuditService auditService) {
        this.auditService = auditService;

        // Đăng ký các entity class được audit
        entityClassMap = new HashMap<>();
        entityClassMap.put("room", Room.class);
        entityClassMap.put("room-category", RoomCategory.class);
        entityClassMap.put("user", User.class);
        entityClassMap.put("booking", Booking.class);
    }

    /**
     * Lấy danh sách các entity type được audit
     */
    @GetMapping("/entity-types")
    public ResponseEntity<List<String>> getAuditedEntityTypes() {
        return ResponseEntity.ok(List.copyOf(entityClassMap.keySet()));
    }

    /**
     * Lấy danh sách các phiên bản của một entity
     * 
     * @param entityType Loại entity (room, user, booking, room-category)
     * @param entityId ID của entity
     * @return Danh sách các phiên bản
     */
    @GetMapping("/{entityType}/{entityId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<?> getEntityRevisions(
            @PathVariable("entityType") String entityType,
            @PathVariable("entityId") Long entityId) {
        
        Class<?> entityClass = entityClassMap.get(entityType);
        if (entityClass == null) {
            return ResponseEntity
                    .badRequest()
                    .body("Entity type '" + entityType + "' not supported. Supported types: " + 
                            String.join(", ", entityClassMap.keySet()));
        }
        
        // Sử dụng Class bị raw type để tránh lỗi type mismatch
        List revisions = auditService.getRevisions(entityClass, entityId);
        
        return ResponseEntity.ok(revisions);
    }

    /**
     * Lấy phiên bản cụ thể của một entity
     * 
     * @param entityType Loại entity (room, user, booking, room-category)
     * @param entityId ID của entity
     * @param revisionNumber Số phiên bản
     * @return Phiên bản cụ thể
     */
    @GetMapping("/{entityType}/{entityId}/revision/{revisionNumber}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<?> getEntityRevision(
            @PathVariable("entityType") String entityType,
            @PathVariable("entityId") Long entityId,
            @PathVariable("revisionNumber") int revisionNumber) {
        
        Class<?> entityClass = entityClassMap.get(entityType);
        if (entityClass == null) {
            return ResponseEntity
                    .badRequest()
                    .body("Entity type '" + entityType + "' not supported. Supported types: " + 
                            String.join(", ", entityClassMap.keySet()));
        }
        
        // Sử dụng Class bị raw type để tránh lỗi type mismatch
        EntityRevisionDTO revision = auditService.getRevision(entityClass, entityId, revisionNumber);
        
        if (revision == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(revision);
    }
} 