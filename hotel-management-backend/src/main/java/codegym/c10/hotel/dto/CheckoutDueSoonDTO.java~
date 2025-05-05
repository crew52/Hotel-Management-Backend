package codegym.c10.hotel.dto;

import codegym.c10.hotel.eNum.BookingDetailStatus;
import codegym.c10.hotel.eNum.BookingStatus;
import codegym.c10.hotel.eNum.RoomStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CheckoutDueSoonDTO {
    // Thông tin phòng
    private Long roomId;
    private String roomName;
    private Integer floor;
    private String roomCategory;
    private RoomStatus roomStatus;
    private Boolean isClean;

    // Thông tin booking
    private Long bookingId;
    private LocalDateTime bookingTime;
    private BookingStatus bookingStatus;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;

    // Thông tin booking detail
    private Long bookingDetailId;
    private String rentType;
    private Integer duration;
    private BigDecimal price;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private BookingDetailStatus status;
    private Integer adultCount;
    private Integer childCount;
    private Long remainingMinutes;  // Thời gian còn lại (phút)

    // Thông tin khách hàng
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerAddress;
}
