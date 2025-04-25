package codegym.c10.hotel.service.checkin;

import codegym.c10.hotel.dto.BookingResponseDTO;
import codegym.c10.hotel.dto.RoomBookingDetailsDTO;
import codegym.c10.hotel.dto.RoomBookingRequestDTO;
import codegym.c10.hotel.dto.WalkInRequestDTO;
import codegym.c10.hotel.eNum.BookingDetailStatus;
import codegym.c10.hotel.eNum.BookingStatus;
import codegym.c10.hotel.eNum.RentType;
import codegym.c10.hotel.eNum.RoomStatus;
import codegym.c10.hotel.entity.*;
import codegym.c10.hotel.exception.RoomNotAvailableException;
import codegym.c10.hotel.repository.IBookingDetailsRepository;
import codegym.c10.hotel.repository.IBookingRepository;
import codegym.c10.hotel.repository.IRoomRepository;
import codegym.c10.hotel.service.IRoomService;
import codegym.c10.hotel.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckingService {

    @Autowired
    private IRoomService roomService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IBookingRepository bookingRepository;

    @Autowired
    private IBookingDetailsRepository bookingDetailsRepository;

    @Autowired
    private IRoomRepository roomRepository;

    @Transactional
    public BookingResponseDTO createBooking(WalkInRequestDTO walkInRequestDTO, Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Booking booking = new Booking();
        booking.setCustomerName(walkInRequestDTO.getCustomerName());
        booking.setCustomerPhone(walkInRequestDTO.getCustomerPhone());
        booking.setCustomerNote(walkInRequestDTO.getNote());
        booking.setPaidAmount(walkInRequestDTO.getPaidAmount());
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setCreatedBy(user);
        bookingRepository.save(booking);

        List<RoomBookingDetailsDTO> roomDetailsList = new ArrayList<>();
        for (RoomBookingRequestDTO roomRequest : walkInRequestDTO.getRooms()) {
            Optional<Room> roomOptional = roomService.findByIdAndStatusAndIsCleanTrueAndDeletedFalse(roomRequest.getRoomId());

            if (!roomOptional.isPresent()) {
                throw new RoomNotAvailableException("Room with ID " + roomRequest.getRoomId() + " is not suitable (not available, not clean, or does not exist).");
            }

            Room room = roomOptional.get();
            BookingDetail bookingDetails = new BookingDetail();
            Booking bookingEntity = bookingRepository.findById(booking.getId())
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            bookingDetails.setBooking(bookingEntity);
            bookingDetails.setRoom(room);

            // Sử dụng checkinTime trực tiếp từ roomRequest
            LocalDateTime checkinTime = roomRequest.getCheckinTime();
            bookingDetails.setCheckinTime(checkinTime);

            // Tính toán thời gian checkout dựa trên loại thuê phòng và thời gian thuê
            LocalDateTime checkoutTime = checkinTime; // Bắt đầu từ checkinTime

//            if ("HOUSE".equals(roomRequest.getRentType())) {
//                checkoutTime = checkoutTime.plusHours(roomRequest.getDuration()); // HOUSE: Cộng thêm thời gian thuê theo giờ
//            } else if ("DAILY".equals(roomRequest.getRentType())) {
//                checkoutTime = checkoutTime.plusDays(roomRequest.getDuration()); // DAILY: Cộng thêm thời gian thuê theo ngày
//            } else if ("OVERNIGHT".equals(roomRequest.getRentType())) {
//                // OVERNIGHT: Đặt thời gian checkout vào 6 giờ sáng ngày hôm sau
//                checkoutTime = checkoutTime.toLocalDate().plusDays(1).atTime(6, 0); // 6 AM ngày hôm sau
//            }

//            // Kiểm tra xem thời gian checkout có ít nhất 1 giờ sau check-in không
//            if (checkoutTime.isBefore(checkinTime.plusHours(1))) {
//                throw new IllegalArgumentException("Checkout time must be at least 1 hour later than check-in time.");
//            }

            bookingDetails.setCheckoutTime(checkoutTime.plusHours(1)); // Set thời gian checkout đã tính toán
            bookingDetails.setRentType(roomRequest.getRentType());
            bookingDetails.setDuration(roomRequest.getDuration());
            bookingDetails.setPrice(calculatePrice(room, roomRequest.getRentType(), roomRequest.getDuration()));
            bookingDetails.setStatus(BookingDetailStatus.BOOKED);

            bookingDetailsRepository.save(bookingDetails);

//            room.setStatus(RoomStatus.IN_USE);
//            roomRepository.save(room);

            // Thêm thông tin phòng vào response DTO
            RoomBookingDetailsDTO roomBookingDetailsDTO = new RoomBookingDetailsDTO();
            roomBookingDetailsDTO.setRoomId(room.getId());
            roomBookingDetailsDTO.setRoomCategoryName(room.getRoomCategory().getName());
            roomBookingDetailsDTO.setRentType(bookingDetails.getRentType());
            // Kiểm tra rentType và lấy giá tương ứng
            if (bookingDetails.getRentType() == RentType.HOURLY) {
                roomBookingDetailsDTO.setPrice(room.getRoomCategory().getHourlyPrice());
            }else if (bookingDetails.getRentType() == RentType.DAILY) {
                roomBookingDetailsDTO.setPrice(room.getRoomCategory().getDailyPrice());
            }else {
                roomBookingDetailsDTO.setPrice(room.getRoomCategory().getOvernightPrice());
            }
            roomBookingDetailsDTO.setPriceTotal(bookingDetails.getPrice());
            roomBookingDetailsDTO.setStatus(bookingDetails.getStatus());
            roomBookingDetailsDTO.setCheckinTime(checkinTime);
            roomBookingDetailsDTO.setCheckoutTime(checkoutTime); // Thời gian checkout đã tính toán
            roomBookingDetailsDTO.setDuration(roomRequest.getDuration());
            roomDetailsList.add(roomBookingDetailsDTO);
        }

        // Tạo và trả về BookingResponseDTO
        BookingResponseDTO bookingResponseDTO = new BookingResponseDTO();
        bookingResponseDTO.setBookingId(booking.getId());
        bookingResponseDTO.setCustomerName(booking.getCustomerName());
        bookingResponseDTO.setCustomerPhone(booking.getCustomerPhone());
        bookingResponseDTO.setCustomerNote(booking.getCustomerNote());
        bookingResponseDTO.setPaidAmount(booking.getPaidAmount());
        bookingResponseDTO.setBookingStatus(booking.getBookingStatus());
        bookingResponseDTO.setRooms(roomDetailsList);
        bookingResponseDTO.setBookingCreatedAt(booking.getCreatedAt()); // Thời gian tạo booking
        bookingResponseDTO.setCreatedBy(userId); // Người tạo booking

        return bookingResponseDTO; // Trả về BookingResponseDTO đã tùy chỉnh
    }

    public BigDecimal calculatePrice(Room room, RentType rentType, int duration) {
        RoomCategory roomCategory = room.getRoomCategory();
        BigDecimal price = BigDecimal.ZERO;

        // Tính giá dựa trên rentType
        switch (rentType) {
            case HOURLY:
                price = roomCategory.getHourlyPrice().multiply(BigDecimal.valueOf(duration));
                break;
            case DAILY:
                price = roomCategory.getDailyPrice().multiply(BigDecimal.valueOf(duration));
                break;
            case OVERNIGHT:
                price = roomCategory.getOvernightPrice().multiply(BigDecimal.valueOf(duration));
                break;
            default:
                throw new IllegalArgumentException("Invalid RentType");
        }

        return price.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}

