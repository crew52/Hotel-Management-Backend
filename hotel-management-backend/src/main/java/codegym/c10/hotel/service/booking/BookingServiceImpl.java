package codegym.c10.hotel.service.booking;

import codegym.c10.hotel.annotation.LogActivity;
import codegym.c10.hotel.dto.BookingResponseDTO;
import codegym.c10.hotel.dto.LateCheckinStatusDTO;
import codegym.c10.hotel.dto.RoomBookingDetailsDTO;
import codegym.c10.hotel.dto.RoomLateCheckinStatusDTO;
import codegym.c10.hotel.dto.auth.checkin.CheckinRequestDTO;
import codegym.c10.hotel.dto.auth.checkin.CheckinResponseDTO;
import codegym.c10.hotel.eNum.BookingDetailStatus;
import codegym.c10.hotel.eNum.RoomStatus;
import codegym.c10.hotel.entity.*;
import codegym.c10.hotel.exception.BookingException;
import codegym.c10.hotel.repository.IBookingRepository;
import codegym.c10.hotel.service.user.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * BookingServiceImpl provides implementation for booking-related operations
 * such as retrieving booking details, checking late check-ins, and processing check-ins.
 */
@Service
public class BookingServiceImpl implements IBookingService {
    @Autowired
    private IBookingRepository bookingRepository;

    @Autowired
    private IUserService userService;

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
     * Retrieves a detailed booking response by booking ID, including customer info,
     * booking status, total amount, and room details.
     *
     * @param id Booking ID
     * @return BookingResponseDTO with detailed information, or null if not found
     */
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

            User user = getUserById(booking.getCreatedBy().getId());
            if (user == null) {
                throw new BookingException("User with ID " + booking.getCreatedBy().getId() + " not found.");
            }

            responseDTO.setUsername(user.getUsername());

            // Map room booking details and calculate total amount
            List<RoomBookingDetailsDTO> roomDetailsDTO = booking.getBookingDetails().stream()
                    .map(this::convertToRoomBookingDetailsDTO)
                    .collect(Collectors.toList());

            BigDecimal totalAmount = roomDetailsDTO.stream()
                    .map(RoomBookingDetailsDTO::getPriceTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            responseDTO.setTotalAmount(totalAmount); // <-- gán giá trị tính được
            responseDTO.setRooms(roomDetailsDTO);

            return responseDTO;
        }

        return null;
    }

    /**
     * Converts a BookingDetail entity to a RoomBookingDetailsDTO.
     *
     * @param bookingDetail The BookingDetail entity
     * @return A RoomBookingDetailsDTO with mapped data
     */
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

    /**
     * Finds a booking by ID where the booking is not marked as deleted.
     *
     * @param id Booking ID
     * @return Optional containing the Booking if found
     */
    @Override
    public Optional<Booking> findByIdAndDeletedFalse(Long id) {
        return bookingRepository.findByIdAndDeletedFalse(id);
    }

    /**
     * Checks whether any of the booked rooms are late for check-in.
     *
     * @param id Booking ID
     * @return LateCheckinStatusDTO with check-in status information, or null if booking not found
     */
    @Override
    public LateCheckinStatusDTO getLateCheckinStatus(Long id) {
        Optional<Booking> bookingOpt = bookingRepository.findByIdAndDeletedFalse(id);
        if (bookingOpt.isEmpty()) {
            return null;
        }

        Booking booking = bookingOpt.get();
        LocalDateTime now = LocalDateTime.now();

        List<BookingDetail> bookedRooms = booking.getBookingDetails().stream()
                .filter(detail -> detail.getStatus() == BookingDetailStatus.BOOKED)
                .toList();

        boolean isLate = bookedRooms.stream()
                .anyMatch(detail -> {
                    LocalDateTime checkinTime = detail.getCheckinTime();
                    return checkinTime != null && now.isAfter(checkinTime);
                });

        LocalDateTime earliestExpectedCheckin = bookedRooms.stream()
                .map(BookingDetail::getCheckinTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        List<RoomLateCheckinStatusDTO> roomStatuses = booking.getBookingDetails().stream()
                .map(detail -> {
                    RoomLateCheckinStatusDTO dto = new RoomLateCheckinStatusDTO();
                    dto.setRoomId(detail.getRoom().getId());
                    dto.setExpectedCheckinTime(detail.getCheckinTime());
                    dto.setStatus(detail.getStatus());
                    dto.setLate(detail.getCheckinTime() != null && now.isAfter(detail.getCheckinTime()) &&
                            detail.getStatus() == BookingDetailStatus.BOOKED);
                    return dto;
                }).collect(Collectors.toList());

        LateCheckinStatusDTO dto = new LateCheckinStatusDTO();
        dto.setBookingId(booking.getId());
        dto.setLateCheckin(isLate);
        dto.setEarliestExpectedCheckin(earliestExpectedCheckin);
        dto.setCurrentTime(now);
        dto.setRoomStatuses(roomStatuses);

        return dto;
    }

    /**
     * Performs the check-in process for specific rooms in a booking.
     *
     * @param request CheckinRequestDTO containing booking ID and room IDs to check-in
     * @return CheckinResponseDTO containing the check-in results
     * @throws ResponseStatusException if the booking does not exist
     */
    @Override
    @LogActivity(action = "ROOM_CHECKIN", description = "Xác nhận nhận phòng cho khách hàng")
    public CheckinResponseDTO checkinRooms(CheckinRequestDTO request) {
        Optional<Booking> bookingOpt = bookingRepository.findByIdAndDeletedFalse(request.getBookingId());
        if (bookingOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking không tồn tại");
        }

        Booking booking = bookingOpt.get();

        List<Long> checkedInRoomIds = new ArrayList<>();
        List<Long> failedRoomIds = new ArrayList<>();

        for (Long roomId : request.getRoomIdsToCheckin()) {
            // Tìm BookingDetail tương ứng với roomId trong booking
            Optional<BookingDetail> matchingDetailOpt = booking.getBookingDetails().stream()
                    .filter(detail -> detail.getRoom().getId().equals(roomId))
                    .findFirst();

            if (matchingDetailOpt.isPresent()) {
                BookingDetail detail = matchingDetailOpt.get();

                // Giả sử chỉ cho phép check-in khi trạng thái là BOOKED
                if (detail.getStatus() == BookingDetailStatus.BOOKED) {
                    detail.setStatus(BookingDetailStatus.IN_USE);
                    detail.setRoomStatus(RoomStatus.IN_USE);
                    checkedInRoomIds.add(roomId);
                } else {
                    failedRoomIds.add(roomId); // Không thể check-in vì không đúng trạng thái
                }
            } else {
                failedRoomIds.add(roomId); // Không tìm thấy phòng trong đơn đặt này
            }
        }

        bookingRepository.save(booking); // Cập nhật DB nếu có thay đổi

        CheckinResponseDTO response = new CheckinResponseDTO();
        response.setBookingId(booking.getId());
        response.setCheckedInRoomIds(checkedInRoomIds);
        response.setFailedRoomIds(failedRoomIds);
        return response;
    }

    /**
     * Performs the cancellation process for specific rooms in a booking.
     *
     * @param request CheckinRequestDTO containing booking ID and room IDs to cancel
     * @return CheckinResponseDTO containing the cancellation results (similar structure to check-in)
     * @throws ResponseStatusException if the booking does not exist
     */
    @Override
    @LogActivity(action = "ROOM_CANCEL", description = "Hủy nhận phòng cho khách hàng")
    public CheckinResponseDTO cancelRooms(CheckinRequestDTO request) {
        Optional<Booking> bookingOpt = bookingRepository.findByIdAndDeletedFalse(request.getBookingId());
        if (bookingOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking không tồn tại");
        }

        Booking booking = bookingOpt.get();

        List<Long> cancelledRoomIds = new ArrayList<>();
        List<Long> failedRoomIds = new ArrayList<>();

        // Consider renaming roomIdsToCheckin in CheckinRequestDTO if it's reused,
        // or create a specific CancelRequestDTO for clarity.
        for (Long roomIdToCancel : request.getRoomIdsToCheckin()) {
            Optional<BookingDetail> matchingDetailOpt = booking.getBookingDetails().stream()
                    .filter(detail -> detail.getRoom() != null && detail.getRoom().getId().equals(roomIdToCancel))
                    .findFirst();

            if (matchingDetailOpt.isPresent()) {
                BookingDetail detail = matchingDetailOpt.get();
                Room room = detail.getRoom(); // Get the associated Room entity

                // Check if the current status allows cancellation
                if (detail.getStatus() == BookingDetailStatus.BOOKED || detail.getStatus() == BookingDetailStatus.IN_USE) {
                    detail.setStatus(BookingDetailStatus.CANCELLED); // Set BookingDetail status
                    // detail.setRoomStatus(RoomStatus.AVAILABLE); // This field on BookingDetail might be redundant if Room.status is the source of truth

                    if (detail != null) {
                        detail.setRoomStatus(RoomStatus.AVAILABLE); // CRITICAL: Set actual Room entity status
                        // If Room is not managed by JPA in this transaction, you might need roomRepository.save(room)
                        // However, if Booking aggregates BookingDetail and BookingDetail aggregates Room with proper cascade,
                        // bookingRepository.save(booking) might be enough. This depends on your entity relationships and cascading rules.
                    } else {
                        // This case should ideally not happen if filter `detail.getRoom() != null` is effective
                        // Or if data integrity ensures Room is always present for a BookingDetail.
                        // Handle error or log if room is unexpectedly null for a cancellable BookingDetail.
                        failedRoomIds.add(roomIdToCancel);
                        continue; // Skip to next room
                    }
                    cancelledRoomIds.add(roomIdToCancel);
                } else {
                    // Room cannot be cancelled due to its current BookingDetail status (e.g., already COMPLETED or CANCELLED)
                    failedRoomIds.add(roomIdToCancel);
                }
            } else {
                // Room not found within this booking's details
                failedRoomIds.add(roomIdToCancel);
            }
        }

        bookingRepository.save(booking); // Save changes to booking and potentially cascaded entities

        CheckinResponseDTO response = new CheckinResponseDTO();
        response.setBookingId(booking.getId());
        // Consider renaming fields in CheckinResponseDTO for this cancel operation
        // e.g., setSuccessfullyCancelledRoomIds instead of setCheckedInRoomIds
        response.setCheckedInRoomIds(cancelledRoomIds);
        response.setFailedRoomIds(failedRoomIds);
        return response;
    }
}
