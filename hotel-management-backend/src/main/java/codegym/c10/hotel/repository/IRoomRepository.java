package codegym.c10.hotel.repository;

import codegym.c10.hotel.eNum.RoomStatus;
import codegym.c10.hotel.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IRoomRepository extends JpaRepository<Room, Long> {
    Page<Room> findAllByDeletedFalse(Pageable pageable);
    Optional<Room> findByIdAndDeletedFalse(Long id);

    @Query("SELECT r FROM Room r " +
            "JOIN r.roomCategory rc " +
            "WHERE r.deleted = false " +
            "AND (:status IS NULL OR r.status = :status) " +
            "AND (:floor IS NULL OR r.floor = :floor) " +
            "AND (" +
            "  :keyword IS NULL OR " +
            "  LOWER(r.note) LIKE %:keyword% OR " +
            "  LOWER(rc.name) LIKE %:keyword% OR " +
            "  LOWER(rc.description) LIKE %:keyword%" +
            ")")
    Page<Room> advancedSearch(
            @Param("keyword") String keyword,
            @Param("status") RoomStatus status,
            @Param("floor") Integer floor,
            Pageable pageable);

}
