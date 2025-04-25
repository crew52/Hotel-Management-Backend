package codegym.c10.hotel.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class WalkInRequestDTO {
    private String customerName;
    private String customerPhone;
    private String note;
    private BigDecimal paidAmount;
    private List<RoomBookingRequestDTO> rooms;
}

