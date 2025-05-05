package codegym.c10.hotel.service.audit;

import codegym.c10.hotel.dto.EntityRevisionDTO;

import java.util.List;

/**
 * Interface cho dịch vụ truy xuất lịch sử thay đổi entity
 */
public interface IAuditService {
    
    /**
     * Lấy danh sách các phiên bản (revisions) của một entity
     * 
     * @param entityClass Lớp của entity
     * @param entityId ID của entity
     * @return Danh sách các phiên bản
     */
    <T> List<EntityRevisionDTO<T>> getRevisions(Class<T> entityClass, Object entityId);
    
    /**
     * Lấy phiên bản cụ thể của một entity
     * 
     * @param entityClass Lớp của entity
     * @param entityId ID của entity
     * @param revisionNumber Số phiên bản
     * @return Phiên bản cụ thể của entity
     */
    <T> EntityRevisionDTO<T> getRevision(Class<T> entityClass, Object entityId, int revisionNumber);
} 