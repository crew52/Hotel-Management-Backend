package codegym.c10.hotel.repository;

import codegym.c10.hotel.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    Page<ActivityLog> findByDeletedFalse(Pageable pageable);
    Page<ActivityLog> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);
    List<ActivityLog> findByDeletedFalse();
    List<ActivityLog> findByUserIdAndDeletedFalse(Long userId);
}

