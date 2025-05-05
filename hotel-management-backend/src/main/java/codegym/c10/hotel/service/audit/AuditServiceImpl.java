package codegym.c10.hotel.service.audit;

import codegym.c10.hotel.audit.AuditRevisionEntity;
import codegym.c10.hotel.dto.EntityRevisionDTO;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của service truy xuất lịch sử thay đổi entity
 */
@Service
public class AuditServiceImpl implements IAuditService {

    private final EntityManager entityManager;

    public AuditServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Lấy danh sách các phiên bản (revisions) của một entity
     */
    @Override
    @Transactional(readOnly = true)
    public <T> List<EntityRevisionDTO<T>> getRevisions(Class<T> entityClass, Object entityId) {
        try {
            AuditReader auditReader = AuditReaderFactory.get(entityManager);
            
            // Tạo truy vấn audit
            AuditQuery query = auditReader.createQuery()
                    .forRevisionsOfEntity(entityClass, false, true)
                    .add(AuditEntity.id().eq(entityId))
                    .addOrder(AuditEntity.revisionNumber().desc());
            
            List<Object[]> resultList = query.getResultList();
            if (resultList.isEmpty()) {
                return Collections.emptyList();
            }
            
            // Chuyển đổi kết quả thành DTO
            return resultList.stream()
                    .map(objects -> {
                        @SuppressWarnings("unchecked")
                        T entity = (T) objects[0];
                        AuditRevisionEntity revisionEntity = (AuditRevisionEntity) objects[1];
                        RevisionType revisionType = (RevisionType) objects[2];
                        
                        return EntityRevisionDTO.<T>builder()
                                .revisionNumber(revisionEntity.getId())
                                .revisionDate(revisionEntity.getTimestamp())
                                .username(revisionEntity.getUsername())
                                .userId(revisionEntity.getUserId())
                                .revisionType(revisionType.name())
                                .entity(entity)
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Log lỗi và trả về danh sách rỗng
            return new ArrayList<>();
        }
    }

    /**
     * Lấy phiên bản cụ thể của một entity
     */
    @Override
    @Transactional(readOnly = true)
    public <T> EntityRevisionDTO<T> getRevision(Class<T> entityClass, Object entityId, int revisionNumber) {
        try {
            AuditReader auditReader = AuditReaderFactory.get(entityManager);
            
            // Kiểm tra xem revision có tồn tại không
            if (!auditReader.isEntityClassAudited(entityClass)) {
                return null;
            }
            
            T entity = auditReader.find(entityClass, entityId, revisionNumber);
            if (entity == null) {
                return null;
            }
            
            // Lấy thông tin revision
            AuditRevisionEntity revisionEntity = auditReader.findRevision(AuditRevisionEntity.class, revisionNumber);
            
            // Xác định loại thay đổi (khó xác định chính xác cho một revision cụ thể)
            String revisionType = "UNKNOWN";
            
            // Tìm kiếm revision trước đó để xác định loại thay đổi
            List<Number> revisions = auditReader.getRevisions(entityClass, entityId);
            int index = revisions.indexOf(revisionNumber);
            
            if (index == 0) {
                revisionType = "ADD"; // Revision đầu tiên => thêm mới
            } else if (index > 0 && index < revisions.size() - 1) {
                revisionType = "MOD"; // Revision ở giữa => cập nhật
            } else if (index == revisions.size() - 1) {
                // Kiểm tra xem có phải là xóa không bằng cách thử lấy entity hiện tại
                try {
                    auditReader.find(entityClass, entityId, revisionNumber + 1);
                } catch (Exception e) {
                    revisionType = "DEL"; // Không tìm thấy => đã xóa
                }
            }
            
            return EntityRevisionDTO.<T>builder()
                    .revisionNumber(revisionNumber)
                    .revisionDate(revisionEntity.getTimestamp())
                    .username(revisionEntity.getUsername())
                    .userId(revisionEntity.getUserId())
                    .revisionType(revisionType)
                    .entity(entity)
                    .build();
        } catch (Exception e) {
            // Log lỗi và trả về null
            return null;
        }
    }
} 