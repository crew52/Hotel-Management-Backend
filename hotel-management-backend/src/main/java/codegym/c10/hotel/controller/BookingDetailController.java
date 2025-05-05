package codegym.c10.hotel.controller;

import codegym.c10.hotel.eNum.RoomStatus;
import codegym.c10.hotel.entity.BookingDetail;
import codegym.c10.hotel.service.bookingdetail.IBookingDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/booking-detail")
public class BookingDetailController {

    @Autowired
    private IBookingDetailsService bookingDetailsService;

    @PatchMapping("/{id}/room-status")
    public ResponseEntity<?> updateRoomStatusWithParam(@PathVariable Long id,
                                                       @RequestParam RoomStatus roomStatus) {
        BookingDetail bookingDetail = bookingDetailsService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "BookingDetail not found"));

        bookingDetail.setRoomStatus(roomStatus);
        bookingDetailsService.save(bookingDetail);

        return ResponseEntity.ok().body("Room status updated to " + roomStatus);
    }
}
