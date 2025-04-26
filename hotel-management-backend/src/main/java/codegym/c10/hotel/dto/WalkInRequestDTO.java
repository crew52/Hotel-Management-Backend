package codegym.c10.hotel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WalkInRequestDTO {

    @Size(max = 100)
    private String customerName;

    @Size(max = 20)
    private String customerPhone;

    private String note;

    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal paidAmount;

    @NotEmpty(message = "Phải chọn ít nhất một phòng để đặt.")
    private List<@Valid RoomBookingRequestDTO> rooms;
}


