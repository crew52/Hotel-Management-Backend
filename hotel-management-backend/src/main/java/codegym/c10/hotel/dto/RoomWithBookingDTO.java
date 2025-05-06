package codegym.c10.hotel.dto;

import codegym.c10.hotel.eNum.RoomStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class RoomWithBookingDTO {
    private Long id;
    private String roomCategoryName;
    private Integer floor;
    private RoomStatus status;
    private Boolean isClean;
    private String note;
    private LocalDate startDate;
    private Integer checkInDuration;
    // Thông tin về booking
    private List<SimpleBookingDetailDTO> bookings;
}