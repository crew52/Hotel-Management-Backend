package codegym.c10.hotel.service.booking;

import codegym.c10.hotel.dto.LateCheckinStatusDTO;
import codegym.c10.hotel.entity.Booking;

import java.util.Optional;

public interface IBookingService {
    Optional<Booking> findByIdAndDeletedFalse(Long id);
    LateCheckinStatusDTO getLateCheckinStatus(Long id);
}
