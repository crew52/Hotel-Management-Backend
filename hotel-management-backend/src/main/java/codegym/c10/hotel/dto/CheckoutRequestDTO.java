package codegym.c10.hotel.dto;

import lombok.Data;

@Data
public class CheckoutRequestDTO {
    private Long roomId;
    private Long bookingId;
    private Boolean isClean;
    private Long userId;
    private String userName;
}
