package codegym.c10.hotel.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RoomInvoiceDTO {
    // Thông tin phòng
    private Long roomId;
    private String roomName;
    private Integer floor;
    private String roomCategoryName;

    // Thông tin thuê phòng
    private String rentType;        // HOURLY/DAILY/OVERNIGHT
    private int duration;
    private BigDecimal unitPrice;
    private BigDecimal totalFee;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;

    // Thông tin khách trong phòng
    private Integer adultCount;
    private Integer childCount;
}
