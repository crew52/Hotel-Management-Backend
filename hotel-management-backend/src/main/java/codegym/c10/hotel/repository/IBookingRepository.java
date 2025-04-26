package codegym.c10.hotel.repository;

import codegym.c10.hotel.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBookingRepository extends JpaRepository<Booking, Long> {
}
