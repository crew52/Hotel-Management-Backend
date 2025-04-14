package codegym.c10.hotel.repository;

import codegym.c10.hotel.entity.RoomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IRoomCategoryRepository extends JpaRepository<RoomCategory, Long> {
    List<RoomCategory> findByDeletedFalse();
    Optional<RoomCategory> findByIdAndDeletedFalse(Long id);
    boolean existsByCode(String code);
}
