package codegym.c10.hotel.service.booking;

import codegym.c10.hotel.dto.BookingResponseDTO;
import codegym.c10.hotel.dto.RoomBookingDetailsDTO;
import codegym.c10.hotel.eNum.RentType;
import codegym.c10.hotel.entity.*;
import codegym.c10.hotel.repository.IBookingRepository;
import codegym.c10.hotel.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements IBookingService{
    @Autowired
    private IBookingRepository bookingRepository;

    public BookingResponseDTO getBookingResponse(Long id) {
        Optional<Booking> bookingOpt = bookingRepository.findByIdAndDeletedFalse(id);

        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();

            BookingResponseDTO responseDTO = new BookingResponseDTO();
            responseDTO.setBookingId(booking.getId());
            responseDTO.setCustomerName(booking.getCustomer().getFullName());
            responseDTO.setCustomerPhone(booking.getCustomer().getPhone());
            responseDTO.setNote(booking.getNote());
            responseDTO.setPaidAmount(booking.getPaidAmount());
            responseDTO.setBookingStatus(booking.getBookingStatus());
            responseDTO.setBookingCreatedAt(booking.getCreatedAt());
            responseDTO.setCreatedBy(booking.getCreatedBy().getId());

            // Tính tổng priceTotal và map sang RoomBookingDetailsDTO
            List<RoomBookingDetailsDTO> roomDetailsDTO = booking.getBookingDetails().stream()
                    .map(this::convertToRoomBookingDetailsDTO)
                    .collect(Collectors.toList());

            // Tính tổng từ các roomDetailsDTO
            BigDecimal totalAmount = roomDetailsDTO.stream()
                    .map(RoomBookingDetailsDTO::getPriceTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            responseDTO.setTotalAmount(totalAmount); // <-- gán giá trị tính được
            responseDTO.setRooms(roomDetailsDTO);

            return responseDTO;
        }

        return null;
    }

    private RoomBookingDetailsDTO convertToRoomBookingDetailsDTO(BookingDetail bookingDetail) {
        RoomBookingDetailsDTO roomDTO = new RoomBookingDetailsDTO();
        roomDTO.setRoomId(bookingDetail.getRoom().getId());
        roomDTO.setRoomCategoryName(bookingDetail.getRoom().getRoomCategory().getName());
        roomDTO.setAdultCount(bookingDetail.getAdultCount());
        roomDTO.setChildCount(bookingDetail.getChildCount());
        roomDTO.setRentType(bookingDetail.getRentType());
        roomDTO.setPrice(bookingDetail.getPrice());
        roomDTO.setPriceTotal(bookingDetail.getPrice().multiply(BigDecimal.valueOf(bookingDetail.getDuration())));
        roomDTO.setStatus(bookingDetail.getStatus());
        roomDTO.setCheckinTime(bookingDetail.getCheckinTime());
        roomDTO.setCheckoutTime(bookingDetail.getCheckoutTime());
        roomDTO.setDuration(bookingDetail.getDuration());

        return roomDTO;
    }

    @Override
    public Optional<Booking> findByIdAndDeletedFalse(Long id) {
        return bookingRepository.findByIdAndDeletedFalse(id);
    }
}
