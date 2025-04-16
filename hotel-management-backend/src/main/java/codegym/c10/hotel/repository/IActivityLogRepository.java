package codegym.c10.hotel.repository;

import codegym.c10.hotel.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    Page<ActivityLog> findByDeletedFalse(Pageable pageable);
}

