package codegym.c10.hotel.service.checkout;

import codegym.c10.hotel.dto.CheckoutRequestDTO;
import codegym.c10.hotel.eNum.RoomStatus;
import codegym.c10.hotel.entity.Booking;
import codegym.c10.hotel.entity.Room;
import codegym.c10.hotel.repository.IBookingRepository;
import codegym.c10.hotel.repository.IRoomRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CheckoutService {

    @Autowired
    private IRoomRepository roomRepository;

    @Autowired
    private IBookingRepository bookingRepository;

    @Transactional
    public void processCheckout(CheckoutRequestDTO requestDTO) {
        Booking booking = bookingRepository.findById(requestDTO.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));
        booking.setCheckoutTime(LocalDateTime.now());
        bookingRepository.save(booking);

        Room room = roomRepository.findById(requestDTO.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room không tồn tại"));
        room.setIsClean(requestDTO.getIsClean());
        room.setStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room);
    }
}