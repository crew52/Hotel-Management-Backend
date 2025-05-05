package codegym.c10.hotel.service.bookingdetail;

import codegym.c10.hotel.entity.BookingDetail;
import codegym.c10.hotel.repository.IBookingDetailsRepository;
import codegym.c10.hotel.repository.IBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookingDetailsServiceImpl implements IBookingDetailsService{

    @Autowired
    private IBookingDetailsRepository bookingDetailsRepository;

    @Override
    public Iterable<BookingDetail> findAll() {
        return null;
    }

    @Override
    public BookingDetail save(BookingDetail T) {
        return bookingDetailsRepository.save(T);
    }

    @Override
    public Optional<BookingDetail> findById(Long id) {
        return bookingDetailsRepository.findByIdAndDeletedFalse(id);
    }

    @Override
    public void remove(Long id) {

    }
}
