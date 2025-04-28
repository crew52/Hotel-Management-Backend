package codegym.c10.hotel.repository;

import codegym.c10.hotel.entity.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IBookingDetailsRepository extends JpaRepository<BookingDetail, Long> {
    @Query("""
                SELECT CASE WHEN COUNT(bd) > 0 THEN true ELSE false END
                FROM BookingDetail bd
                WHERE bd.room.id = :roomId
                  AND bd.status = 'BOOKED'
                  AND bd.checkinTime < :checkoutTime
                  AND bd.checkoutTime > :checkinTime
            """)
    boolean existsByRoomIdAndTimeOverlap(@Param("roomId") Long roomId,
                                         @Param("checkinTime") LocalDateTime checkinTime,
                                         @Param("checkoutTime") LocalDateTime checkoutTime);
}