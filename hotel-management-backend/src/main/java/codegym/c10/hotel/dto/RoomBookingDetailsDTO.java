package codegym.c10.hotel.dto;

import codegym.c10.hotel.eNum.BookingDetailStatus;
import codegym.c10.hotel.eNum.RentType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RoomBookingDetailsDTO {
    private Long roomId;
    private String roomCategoryName;
    private Integer adultCount;
    private Integer childCount;
    private RentType rentType;
    private BigDecimal price;
    private BigDecimal priceTotal;
    private BookingDetailStatus status;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private Integer duration;
}
