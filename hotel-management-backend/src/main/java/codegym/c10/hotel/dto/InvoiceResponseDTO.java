package codegym.c10.hotel.dto;

import codegym.c10.hotel.eNum.BookingStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class InvoiceResponseDTO {
    // Thông tin booking
    private Long bookingId;
    private LocalDateTime bookingTime;
    private BookingStatus bookingStatus;
    private String note;

    // Thông tin khách hàng
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String customerAddress;

    // Thông tin thanh toán
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;  // Số tiền còn lại phải trả

    // Chi tiết các phòng
    private List<RoomInvoiceDTO> rooms;
    private BigDecimal roomFee;      // Tổng tiền phòng
    private BigDecimal serviceFee;      // Phí dịch vụ
    private BigDecimal surcharge;    // Phụ phí
    private BigDecimal totalFee;     // Tổng cộng

    // Thông tin người tạo
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
}