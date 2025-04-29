package codegym.c10.hotel.dto;

import codegym.c10.hotel.eNum.BookingDetailStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FeeResponseDTO {
    // Thông tin BookingDetail
    private Long bookingDetailId;
    private String rentType;        // HOURLY/DAILY/OVERNIGHT
    private int duration;
    private BigDecimal unitPrice;
    private BigDecimal totalFee;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private BookingDetailStatus status;

    // Thông tin Room
    private Long roomId;
    private String roomName;        // note từ bảng Room
    private Integer floor;
    private String roomCategoryName;
    private BigDecimal hourlyPrice;
    private BigDecimal dailyPrice;
    private BigDecimal overnightPrice;

    // Thông tin Customer từ Booking
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    // Thông tin Booking
    private Long bookingId;
    private LocalDateTime bookingTime;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private String bookingStatus;

    // Thông tin thêm
    private Integer adultCount;     // Số người lớn
    private Integer childCount;     // Số trẻ em
}