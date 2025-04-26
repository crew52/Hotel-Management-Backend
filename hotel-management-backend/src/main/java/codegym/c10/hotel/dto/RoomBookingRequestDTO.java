package codegym.c10.hotel.dto;

import codegym.c10.hotel.eNum.RentType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RoomBookingRequestDTO {

    @NotNull(message = "Phòng không được để trống.")
    private Long roomId;

    @NotNull(message = "Thời gian check-in không được để trống.")
    private LocalDateTime checkinTime;

    @NotNull(message = "Loại thuê không được để trống.")
    private RentType rentType;

    @Min(value = 1, message = "Thời lượng phải ít nhất là 1.")
    private int duration;
}
