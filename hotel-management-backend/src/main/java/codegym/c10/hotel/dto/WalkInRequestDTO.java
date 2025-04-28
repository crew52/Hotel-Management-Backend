package codegym.c10.hotel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WalkInRequestDTO {

    @NotNull(message = "Customer không được để trống.")
    private Long customerId;

    private String note;

    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal paidAmount;

    @NotEmpty(message = "Phải chọn ít nhất một phòng để đặt.")
    private List<@Valid RoomBookingRequestDTO> rooms;
}


