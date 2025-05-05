package codegym.c10.hotel.service.bookingdetail;

import codegym.c10.hotel.entity.Booking;
import codegym.c10.hotel.entity.BookingDetail;
import codegym.c10.hotel.entity.Customer;
import codegym.c10.hotel.service.IGenerateService;

import java.util.Optional;

public interface IBookingDetailsService extends IGenerateService<BookingDetail> {
    Optional<BookingDetail> findById(Long id);
}
