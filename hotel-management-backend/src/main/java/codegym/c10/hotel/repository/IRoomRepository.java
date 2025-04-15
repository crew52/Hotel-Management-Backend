package codegym.c10.hotel.repository;

import codegym.c10.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IRoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByDeletedFalse();
    Optional<Room> findByIdAndDeletedFalse(Long id);
}
