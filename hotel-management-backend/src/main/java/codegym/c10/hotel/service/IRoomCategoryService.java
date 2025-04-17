package codegym.c10.hotel.service;

import codegym.c10.hotel.eNum.RoomCategoryStatus;
import codegym.c10.hotel.entity.RoomCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IRoomCategoryService {
    Iterable<RoomCategory> findAll();

    RoomCategory save(RoomCategory roomCategory);

    Optional<RoomCategory> findById(Long id);

    void remove(Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    RoomCategory update(RoomCategory roomCategory);

    Page<RoomCategory> advancedSearch(
            String keyword,
            RoomCategoryStatus status,
            Double minHourlyPrice,
            Double maxHourlyPrice,
            Double minDailyPrice,
            Double maxDailyPrice,
            Double minOvernightPrice,
            Double maxOvernightPrice,
            Pageable pageable);
}