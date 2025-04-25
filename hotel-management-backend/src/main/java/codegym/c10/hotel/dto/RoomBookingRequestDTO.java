package codegym.c10.hotel.dto;

import codegym.c10.hotel.eNum.RentType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoomBookingRequestDTO {
    private Long roomId;
    private LocalDateTime checkinTime;
    private LocalDateTime checkoutTime;
    private RentType rentType;
    private int duration;
}