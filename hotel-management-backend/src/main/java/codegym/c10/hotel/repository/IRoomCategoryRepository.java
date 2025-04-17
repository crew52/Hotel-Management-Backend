package codegym.c10.hotel.repository;

import codegym.c10.hotel.eNum.RoomCategoryStatus;
import codegym.c10.hotel.entity.RoomCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IRoomCategoryRepository extends JpaRepository<RoomCategory, Long> {
    List<RoomCategory> findByDeletedFalse();
    Optional<RoomCategory> findByIdAndDeletedFalse(Long id);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT rc FROM RoomCategory rc " +
            "WHERE rc.deleted = false " +
            "AND (:keyword IS NULL OR " +
            "LOWER(rc.code) LIKE %:keyword% OR " +
            "LOWER(rc.name) LIKE %:keyword% OR " +
            "LOWER(rc.description) LIKE %:keyword%) " +
            "AND (:status IS NULL OR rc.status = :status) " +
            "AND (:minHourlyPrice IS NULL OR rc.hourlyPrice >= :minHourlyPrice) " +
            "AND (:maxHourlyPrice IS NULL OR rc.hourlyPrice <= :maxHourlyPrice) " +
            "AND (:minDailyPrice IS NULL OR rc.dailyPrice >= :minDailyPrice) " +
            "AND (:maxDailyPrice IS NULL OR rc.dailyPrice <= :maxDailyPrice) " +
            "AND (:minOvernightPrice IS NULL OR rc.overnightPrice >= :minOvernightPrice) " +
            "AND (:maxOvernightPrice IS NULL OR rc.overnightPrice <= :maxOvernightPrice)")
    Page<RoomCategory> advancedSearch(
            @Param("keyword") String keyword,
            @Param("status") RoomCategoryStatus status,
            @Param("minHourlyPrice") Double minHourlyPrice,
            @Param("maxHourlyPrice") Double maxHourlyPrice,
            @Param("minDailyPrice") Double minDailyPrice,
            @Param("maxDailyPrice") Double maxDailyPrice,
            @Param("minOvernightPrice") Double minOvernightPrice,
            @Param("maxOvernightPrice") Double maxOvernightPrice,
            Pageable pageable);
}