package codegym.c10.hotel.dto;

import codegym.c10.hotel.eNum.BookingDetailStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RoomLateCheckinStatusDTO {
    private Long roomId;
    private LocalDateTime expectedCheckinTime;
    private boolean isLate;
    private BookingDetailStatus status;
}