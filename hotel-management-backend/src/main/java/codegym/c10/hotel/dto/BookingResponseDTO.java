package codegym.c10.hotel.dto;

import codegym.c10.hotel.eNum.BookingStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookingResponseDTO {
    private Long bookingId;
    private String customerName;
    private String customerPhone;
    private String note;
    private BigDecimal paidAmount;
    private BigDecimal totalAmount;
    private BookingStatus bookingStatus;
    private List<RoomBookingDetailsDTO> rooms;
    private LocalDateTime bookingCreatedAt;
    private Long createdBy;
}
