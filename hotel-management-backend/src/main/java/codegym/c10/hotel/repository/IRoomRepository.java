package codegym.c10.hotel.repository;

import codegym.c10.hotel.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IRoomRepository extends JpaRepository<Room, Long> {
    Page<Room> findAllByDeletedFalse(Pageable pageable);
    Optional<Room> findByIdAndDeletedFalse(Long id);
}
