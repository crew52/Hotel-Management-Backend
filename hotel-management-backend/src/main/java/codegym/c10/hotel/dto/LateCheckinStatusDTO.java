package codegym.c10.hotel.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class    LateCheckinStatusDTO {
    private Long bookingId;
    private boolean isLateCheckin;
    private LocalDateTime earliestExpectedCheckin;
    private LocalDateTime currentTime;
    private List<RoomLateCheckinStatusDTO> roomStatuses; // <-- thêm mới
}