package codegym.c10.hotel.repository;

import codegym.c10.hotel.entity.RoomCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRoomCategoryRepository extends JpaRepository<RoomCategory, Long> {
}
