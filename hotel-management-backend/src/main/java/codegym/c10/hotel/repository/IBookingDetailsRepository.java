package codegym.c10.hotel.repository;

import codegym.c10.hotel.entity.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBookingDetailsRepository extends JpaRepository<BookingDetail, Long> {
}