package codegym.c10.hotel.dto.auth.checkin;

import lombok.Data;

import java.util.List;

@Data
public class CheckinResponseDTO {
    private Long bookingId;
    private List<Long> checkedInRoomIds;
    private List<Long> failedRoomIds;
}
