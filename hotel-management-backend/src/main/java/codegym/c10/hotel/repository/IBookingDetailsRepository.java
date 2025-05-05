package codegym.c10.hotel.repository;

import codegym.c10.hotel.eNum.BookingDetailStatus;
import codegym.c10.hotel.entity.Booking;
import codegym.c10.hotel.entity.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    /**
     * Tìm kiếm các chi tiết đặt phòng (BookingDetail) sắp đến hạn trả phòng
     *
     * @Query - Định nghĩa câu truy vấn JPQL (Java Persistence Query Language)
     * - SELECT bd FROM BookingDetail bd: Chọn các booking detail
     *
     * - JOIN FETCH: Sử dụng để tải dữ liệu của các đối tượng liên quan ngay lập tức (eager loading)
     *   + JOIN FETCH bd.room r: Lấy thông tin phòng liên quan
     *   + JOIN FETCH r.roomCategory rc: Lấy thông tin loại phòng
     *   + JOIN FETCH bd.booking b: Lấy thông tin đặt phòng
     *   + JOIN FETCH b.customer c: Lấy thông tin khách hàng
     *
     * - Điều kiện WHERE:
     *   + bd.checkoutTime BETWEEN :now AND :threshold:
     *     Thời gian trả phòng nằm trong khoảng từ hiện tại đến ngưỡng định trước
     *   + bd.status = :status:
     *     Trạng thái của booking detail (thường là IN_USE)
     *
     * - ORDER BY bd.checkoutTime ASC:
     *   Sắp xếp kết quả theo thời gian checkout tăng dần
     *   (phòng sắp checkout sớm nhất sẽ hiển thị trước)
     *
     * @param now Thời điểm hiện tại
     * @param threshold Thời điểm giới hạn (thường là now + 60 phút)
     * @param status Trạng thái cần tìm (thường là IN_USE)
     * @return Danh sách các BookingDetail thỏa mãn điều kiện
     */

    @Query("SELECT bd FROM BookingDetail bd " +
            "JOIN FETCH bd.room r " +
            "JOIN FETCH r.roomCategory rc " +
            "JOIN FETCH bd.booking b " +
            "JOIN FETCH b.customer c " +
            "WHERE bd.checkoutTime BETWEEN :now AND :threshold " +
            "AND bd.status = :status " +
            "ORDER BY bd.checkoutTime ASC")
    List<BookingDetail> findCheckoutDueSoon(
            @Param("now") LocalDateTime now,
            @Param("threshold") LocalDateTime threshold,
            @Param("status") BookingDetailStatus status
    );

    Optional<BookingDetail> findByIdAndDeletedFalse(Long id);
}