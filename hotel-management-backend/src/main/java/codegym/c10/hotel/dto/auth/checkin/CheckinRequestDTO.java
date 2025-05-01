package codegym.c10.hotel.dto.auth.checkin;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CheckinRequestDTO {
    @NotNull
    private Long bookingId;

    @NotEmpty
    private List<Long> roomIdsToCheckin;
}
