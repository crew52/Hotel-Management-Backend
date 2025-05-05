package codegym.c10.hotel.service.checkin;

import codegym.c10.hotel.dto.*;
import codegym.c10.hotel.eNum.*;
import codegym.c10.hotel.entity.*;
import codegym.c10.hotel.exception.BookingException;
import codegym.c10.hotel.exception.CustomerNotFoundException;
import codegym.c10.hotel.exception.RoomNotAvailableException;
import codegym.c10.hotel.repository.*;
import codegym.c10.hotel.service.IRoomService;
import codegym.c10.hotel.service.customer.ICustomerService;
import codegym.c10.hotel.service.user.IUserService;
import codegym.c10.hotel.validator.GuestCountValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service class responsible for handling booking creation and room assignment logic.
 */
@Service
public class CheckingService {

    @Autowired
    private IRoomService roomService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private IBookingRepository bookingRepository;

    @Autowired
    private IBookingDetailsRepository bookingDetailsRepository;

    /**
     * Creates a new booking based on the walk-in request and user ID.
     *
     * @param walkInRequestDTO the walk-in booking request containing customer and room info
     * @param userId the ID of the user performing the booking
     * @return BookingResponseDTO containing booking details and room information
     */
    @Transactional
    public BookingResponseDTO createBooking(WalkInRequestDTO walkInRequestDTO, Long userId) {
        User user = getUserById(userId);
        Booking booking = createNewBooking(walkInRequestDTO, user);
        List<RoomBookingDetailsDTO> roomDetailsList = processRoomBookings(walkInRequestDTO.getRooms(), booking);

        return buildBookingResponseDTO(booking, roomDetailsList, userId);
    }

    /**
     * Retrieves a User entity by ID.
     *
     * @param userId the ID of the user
     * @return the User entity
     * @throws BookingException if user not found
     */
    private User getUserById(Long userId) {
        return userService.findById(userId)
                .orElseThrow(() -> new BookingException("User with ID " + userId + " not found."));
    }

    /**
     * Creates a new Booking entity based on the walk-in request and the user.
     *
     * @param walkInRequestDTO the booking request data
     * @param user the user who created the booking
     * @return the created Booking entity
     */
    private Booking createNewBooking(WalkInRequestDTO walkInRequestDTO, User user) {
        Customer customer = customerService.findByIdAndDeletedFalse(walkInRequestDTO.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + walkInRequestDTO.getCustomerId()));

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setNote(walkInRequestDTO.getNote());
        booking.setPaidAmount(walkInRequestDTO.getPaidAmount() != null ? walkInRequestDTO.getPaidAmount() : BigDecimal.ZERO);
        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setCreatedBy(user);

        bookingRepository.save(booking);
        return booking;
    }

    /**
     * Processes room booking requests, validates room availability, and creates booking details.
     *
     * @param roomRequests the list of room booking requests
     * @param booking the parent booking
     * @return list of RoomBookingDetailsDTO representing booked rooms
     */
    private List<RoomBookingDetailsDTO> processRoomBookings(List<RoomBookingRequestDTO> roomRequests, Booking booking) {
        List<RoomBookingDetailsDTO> roomDetailsList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (RoomBookingRequestDTO roomRequest : roomRequests) {
            validateCheckinTime(roomRequest.getCheckinTime());

            LocalDateTime checkoutTime = calculateCheckoutTime(roomRequest.getRentType(), roomRequest.getDuration(), roomRequest.getCheckinTime());
            Room room = validateRoomAvailability(roomRequest.getRoomId(), roomRequest.getCheckinTime(), checkoutTime);

            BookingDetail bookingDetails = createBookingDetail(roomRequest, room, booking);
            bookingDetailsRepository.save(bookingDetails);

            RoomBookingDetailsDTO roomBookingDetailsDTO = mapToRoomBookingDetailsDTO(room, bookingDetails, roomRequest);
            roomDetailsList.add(roomBookingDetailsDTO);

            totalAmount = totalAmount.add(bookingDetails.getPrice());
        }

        booking.setTotalAmount(totalAmount);
        bookingRepository.save(booking);

        return roomDetailsList;
    }

    /**
     * Validates that the check-in time is not in the past.
     *
     * @param checkinTime the check-in time to validate
     */
    private void validateCheckinTime(LocalDateTime checkinTime) {
        if (checkinTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Check-in time cannot be in the past.");
        }
    }

    /**
     * Validates room availability and cleanliness before booking.
     *
     * @param roomId the ID of the room
     * @param checkinTime check-in time
     * @param checkoutTime checkout time
     * @return the validated Room entity
     * @throws RoomNotAvailableException if room is unavailable or already booked
     */
    private Room validateRoomAvailability(Long roomId, LocalDateTime checkinTime, LocalDateTime checkoutTime) {
        Room room = roomService.findByIdAndStatusAndIsCleanTrueAndDeletedFalse(roomId)
                .orElseThrow(() -> new RoomNotAvailableException("Room with ID " + roomId + " is not suitable (not available, not clean, or does not exist)."));

        if (bookingDetailsRepository.existsByRoomIdAndTimeOverlap(roomId, checkinTime, checkoutTime)) {
            throw new RoomNotAvailableException("Room with ID " + roomId + " is already booked in the selected time range.");
        }

        return room;
    }

    /**
     * Creates a BookingDetail entity for a room booking request.
     *
     * @param roomRequest the room booking request
     * @param room the room to be booked
     * @param booking the parent booking
     * @return the created BookingDetail entity
     */
    private BookingDetail createBookingDetail(RoomBookingRequestDTO roomRequest, Room room, Booking booking) {
        LocalDateTime checkinTime = roomRequest.getCheckinTime();
        LocalDateTime checkoutTime = calculateCheckoutTime(roomRequest.getRentType(), roomRequest.getDuration(), checkinTime);

        if (checkoutTime.isBefore(checkinTime.plusHours(1))) {
            throw new IllegalArgumentException("Checkout time must be at least 1 hour later than check-in time.");
        }

        GuestCountValidator.validate(room.getRoomCategory(), roomRequest.getAdultCount(), roomRequest.getChildCount());

        BookingDetail bookingDetails = new BookingDetail();
        bookingDetails.setBooking(booking);
        bookingDetails.setRoom(room);
        bookingDetails.setCheckinTime(checkinTime);
        bookingDetails.setCheckoutTime(checkoutTime);
        bookingDetails.setAdultCount(roomRequest.getAdultCount());
        bookingDetails.setChildCount(roomRequest.getChildCount());
        bookingDetails.setRentType(roomRequest.getRentType());
        bookingDetails.setDuration(roomRequest.getDuration());
        bookingDetails.setPrice(calculatePrice(room, roomRequest.getRentType(), roomRequest.getDuration()));
        bookingDetails.setStatus(BookingDetailStatus.BOOKED);
        bookingDetails.setRoomStatus(RoomStatus.UPCOMING);

        return bookingDetails;
    }

    /**
     * Calculates the checkout time based on rent type and duration.
     *
     * @param rentType the type of rent (hourly, daily, overnight)
     * @param duration the rental duration
     * @param checkinTime the initial check-in time
     * @return the calculated checkout time
     */
    private LocalDateTime calculateCheckoutTime(RentType rentType, int duration, LocalDateTime checkinTime) {
        switch (rentType) {
            case HOURLY:
                return checkinTime.plusHours(duration);
            case DAILY:
                return checkinTime.plusDays(duration);
            case OVERNIGHT:
                return checkinTime.toLocalDate().plusDays(1).atTime(6, 0);
            default:
                throw new IllegalArgumentException("Unsupported rent type");
        }
    }

    /**
     * Maps booking details and room information to a RoomBookingDetailsDTO.
     *
     * @param room the room entity
     * @param details the booking detail entity
     * @param roomRequest the original room booking request
     * @return a RoomBookingDetailsDTO representing the booking
     */
    private RoomBookingDetailsDTO mapToRoomBookingDetailsDTO(Room room, BookingDetail details, RoomBookingRequestDTO roomRequest) {
        RoomBookingDetailsDTO dto = new RoomBookingDetailsDTO();
        dto.setRoomId(room.getId());
        dto.setRoomCategoryName(room.getRoomCategory().getName());
        dto.setRentType(details.getRentType());

        switch (details.getRentType()) {
            case HOURLY:
                dto.setPrice(room.getRoomCategory().getHourlyPrice());
                break;
            case DAILY:
                dto.setPrice(room.getRoomCategory().getDailyPrice());
                break;
            case OVERNIGHT:
                dto.setPrice(room.getRoomCategory().getOvernightPrice());
                break;
        }

        dto.setPriceTotal(details.getPrice());
        dto.setStatus(details.getStatus());
        dto.setCheckinTime(details.getCheckinTime());
        dto.setCheckoutTime(details.getCheckoutTime());
        dto.setAdultCount(details.getAdultCount());
        dto.setChildCount(details.getChildCount());
        dto.setDuration(roomRequest.getDuration());
        dto.setRoomStatusBooking(details.getRoomStatus());

        return dto;
    }

    /**
     * Builds a BookingResponseDTO containing booking summary information.
     *
     * @param booking the booking entity
     * @param roomDetailsList the list of booked rooms
     * @param userId the ID of the user who created the booking
     * @return BookingResponseDTO containing full booking details
     */
    private BookingResponseDTO buildBookingResponseDTO(Booking booking, List<RoomBookingDetailsDTO> roomDetailsList, Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            throw new BookingException("User with ID " + userId + " not found.");
        }
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setBookingId(booking.getId());
        dto.setCustomerName(booking.getCustomer().getFullName());
        dto.setCustomerPhone(booking.getCustomer().getPhone());
        dto.setNote(booking.getNote());
        dto.setPaidAmount(booking.getPaidAmount());
        dto.setTotalAmount(booking.getTotalAmount());
        dto.setBookingStatus(booking.getBookingStatus());
        dto.setRooms(roomDetailsList);
        dto.setBookingCreatedAt(booking.getCreatedAt());
        dto.setCreatedBy(userId);
        dto.setUsername(user.getUsername());
        return dto;
    }

    /**
     * Calculates the total price based on rent type and duration.
     *
     * @param room the room entity
     * @param rentType the rent type
     * @param duration the duration of rent
     * @return calculated total price
     */
    public BigDecimal calculatePrice(Room room, RentType rentType, int duration) {
        RoomCategory category = room.getRoomCategory();
        BigDecimal price;

        switch (rentType) {
            case HOURLY:
                price = category.getHourlyPrice().multiply(BigDecimal.valueOf(duration));
                break;
            case DAILY:
                price = category.getDailyPrice().multiply(BigDecimal.valueOf(duration));
                break;
            case OVERNIGHT:
                price = category.getOvernightPrice().multiply(BigDecimal.valueOf(duration));
                break;
            default:
                throw new IllegalArgumentException("Invalid RentType");
        }

        return price.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
