package codegym.c10.hotel.dto;

import codegym.c10.hotel.eNum.RoomStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SimpleBookingDetailDTO {
    private Long id;
    private Long bookingId;
    private RoomStatus roomStatusInBooking;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
}
