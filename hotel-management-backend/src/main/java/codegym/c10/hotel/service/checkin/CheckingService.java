package codegym.c10.hotel.service.checkin;

import codegym.c10.hotel.dto.*;
import codegym.c10.hotel.eNum.*;
import codegym.c10.hotel.entity.*;
import codegym.c10.hotel.exception.RoomNotAvailableException;
import codegym.c10.hotel.repository.*;
import codegym.c10.hotel.service.IRoomService;
import codegym.c10.hotel.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

//@Service
//public class CheckingService {
//
//    @Autowired
//    private IRoomService roomService;
//
//    @Autowired
//    private IUserService userService;
//
//    @Autowired
//    private IBookingRepository bookingRepository;
//
//    @Autowired
//    private IBookingDetailsRepository bookingDetailsRepository;
//
//    @Autowired
//    private IRoomRepository roomRepository;
//
//    @Transactional
//    public BookingResponseDTO createBooking(WalkInRequestDTO walkInRequestDTO, Long userId) {
//        User user = getUserById(userId);
//        Booking booking = createNewBooking(walkInRequestDTO, user);
//        List<RoomBookingDetailsDTO> roomDetailsList = processRoomBookings(walkInRequestDTO.getRooms(), booking);
//
//        return buildBookingResponseDTO(booking, roomDetailsList, userId);
//    }
//
//    private User getUserById(Long userId) {
//        return userService.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
//    }
//
//    private Booking createNewBooking(WalkInRequestDTO walkInRequestDTO, User user) {
//        Booking booking = new Booking();
//        booking.setCustomerName(walkInRequestDTO.getCustomerName());
//        booking.setCustomerPhone(walkInRequestDTO.getCustomerPhone());
//        booking.setCustomerNote(walkInRequestDTO.getNote());
//        booking.setPaidAmount(walkInRequestDTO.getPaidAmount());
//        booking.setBookingStatus(BookingStatus.PENDING);
//        booking.setCreatedBy(user);
//        bookingRepository.save(booking);
//        return booking;
//    }
//
//    private List<RoomBookingDetailsDTO> processRoomBookings(List<RoomBookingRequestDTO> roomRequests, Booking booking) {
//        List<RoomBookingDetailsDTO> roomDetailsList = new ArrayList<>();
//
//        for (RoomBookingRequestDTO roomRequest : roomRequests) {
//            Room room = validateRoomAvailability(roomRequest.getRoomId());
//
//            BookingDetail bookingDetails = createBookingDetail(roomRequest, room, booking);
//            bookingDetailsRepository.save(bookingDetails);
//
//            room.setStatus(RoomStatus.IN_USE);
//            roomRepository.save(room);
//
//            RoomBookingDetailsDTO roomBookingDetailsDTO = mapToRoomBookingDetailsDTO(room, bookingDetails, roomRequest);
//            roomDetailsList.add(roomBookingDetailsDTO);
//        }
//
//        return roomDetailsList;
//    }
//
//    private Room validateRoomAvailability(Long roomId) {
//        return roomService.findByIdAndStatusAndIsCleanTrueAndDeletedFalse(roomId)
//                .orElseThrow(() -> new RoomNotAvailableException("Room with ID " + roomId + " is not suitable (not available, not clean, or does not exist)."));
//    }
//
//    private BookingDetail createBookingDetail(RoomBookingRequestDTO roomRequest, Room room, Booking booking) {
//        LocalDateTime checkinTime = roomRequest.getCheckinTime();
//        LocalDateTime checkoutTime = calculateCheckoutTime(roomRequest.getRentType(), roomRequest.getDuration(), checkinTime);
//
//        if (checkoutTime.isBefore(checkinTime.plusHours(1))) {
//            throw new IllegalArgumentException("Checkout time must be at least 1 hour later than check-in time.");
//        }
//
//        BookingDetail bookingDetails = new BookingDetail();
//        bookingDetails.setBooking(booking);
//        bookingDetails.setRoom(room);
//        bookingDetails.setCheckinTime(checkinTime);
//        bookingDetails.setCheckoutTime(checkoutTime);
//        bookingDetails.setRentType(roomRequest.getRentType());
//        bookingDetails.setDuration(roomRequest.getDuration());
//        bookingDetails.setPrice(calculatePrice(room, roomRequest.getRentType(), roomRequest.getDuration()));
//        bookingDetails.setStatus(BookingDetailStatus.BOOKED);
//
//        return bookingDetails;
//    }
//
//    private LocalDateTime calculateCheckoutTime(RentType rentType, int duration, LocalDateTime checkinTime) {
//        switch (rentType) {
//            case HOURLY:
//                return checkinTime.plusHours(duration);
//            case DAILY:
//                return checkinTime.plusDays(duration);
//            case OVERNIGHT:
//                return checkinTime.toLocalDate().plusDays(1).atTime(6, 0);
//            default:
//                throw new IllegalArgumentException("Unsupported rent type");
//        }
//    }
//
//    private RoomBookingDetailsDTO mapToRoomBookingDetailsDTO(Room room, BookingDetail details, RoomBookingRequestDTO roomRequest) {
//        RoomBookingDetailsDTO dto = new RoomBookingDetailsDTO();
//        dto.setRoomId(room.getId());
//        dto.setRoomCategoryName(room.getRoomCategory().getName());
//        dto.setRentType(details.getRentType());
//
//        switch (details.getRentType()) {
//            case HOURLY:
//                dto.setPrice(room.getRoomCategory().getHourlyPrice());
//                break;
//            case DAILY:
//                dto.setPrice(room.getRoomCategory().getDailyPrice());
//                break;
//            case OVERNIGHT:
//                dto.setPrice(room.getRoomCategory().getOvernightPrice());
//                break;
//        }
//
//        dto.setPriceTotal(details.getPrice());
//        dto.setStatus(details.getStatus());
//        dto.setCheckinTime(details.getCheckinTime());
//        dto.setCheckoutTime(details.getCheckoutTime());
//        dto.setDuration(roomRequest.getDuration());
//
//        return dto;
//    }
//
//    private BookingResponseDTO buildBookingResponseDTO(Booking booking, List<RoomBookingDetailsDTO> roomDetailsList, Long userId) {
//        BookingResponseDTO dto = new BookingResponseDTO();
//        dto.setBookingId(booking.getId());
//        dto.setCustomerName(booking.getCustomerName());
//        dto.setCustomerPhone(booking.getCustomerPhone());
//        dto.setCustomerNote(booking.getCustomerNote());
//        dto.setPaidAmount(booking.getPaidAmount());
//        dto.setBookingStatus(booking.getBookingStatus());
//        dto.setRooms(roomDetailsList);
//        dto.setBookingCreatedAt(booking.getCreatedAt());
//        dto.setCreatedBy(userId);
//        return dto;
//    }
//
//    public BigDecimal calculatePrice(Room room, RentType rentType, int duration) {
//        RoomCategory category = room.getRoomCategory();
//        BigDecimal price;
//
//        switch (rentType) {
//            case HOURLY:
//                price = category.getHourlyPrice().multiply(BigDecimal.valueOf(duration));
//                break;
//            case DAILY:
//                price = category.getDailyPrice().multiply(BigDecimal.valueOf(duration));
//                break;
//            case OVERNIGHT:
//                price = category.getOvernightPrice().multiply(BigDecimal.valueOf(duration));
//                break;
//            default:
//                throw new IllegalArgumentException("Invalid RentType");
//        }
//
//        return price.setScale(2, BigDecimal.ROUND_HALF_UP);
//    }
//}
