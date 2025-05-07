package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.BookingResponseDTO;
import codegym.c10.hotel.dto.LateCheckinStatusDTO;
import codegym.c10.hotel.dto.WalkInRequestDTO;
import codegym.c10.hotel.dto.auth.checkin.CheckinRequestDTO;
import codegym.c10.hotel.dto.auth.checkin.CheckinResponseDTO;
import codegym.c10.hotel.exception.CustomerNotFoundException;
import codegym.c10.hotel.exception.ErrorResponse;
import codegym.c10.hotel.exception.RoomNotAvailableException;
import codegym.c10.hotel.service.AuthenticatedUserService;
import codegym.c10.hotel.service.booking.BookingServiceImpl;
import codegym.c10.hotel.service.checkin.CheckingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller responsible for handling operations related to guest check-in,
 * including walk-in bookings, checking in reserved rooms, and checking late check-in status.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CheckinController {
    @Autowired
    private CheckingService checkingService;

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    /**
     * API endpoint to create a new walk-in booking (booking made directly at the front desk).
     *
     * <p>
     * Workflow:
     * <ul>
     *     <li>Extract user ID from the authenticated request.</li>
     *     <li>Create a new booking using {@link CheckingService}.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Response:
     * <ul>
     *     <li>200 OK if the booking is created successfully.</li>
     *     <li>400 Bad Request if the room is unavailable, customer not found, or input is invalid.</li>
     *     <li>500 Internal Server Error for unexpected system errors.</li>
     * </ul>
     * </p>
     *
     * @param walkInRequestDTO Booking information sent from the client.
     * @param request          HttpServletRequest containing the user authentication info.
     * @return ResponseEntity containing booking status and booking details if successful.
     */
    @PostMapping("/checkins/walkin")
    public ResponseEntity<Map<String, Object>> checkinWalking(
            @Valid @RequestBody WalkInRequestDTO walkInRequestDTO,
            HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = authenticatedUserService.extractUserId(request);

            BookingResponseDTO bookingResponseDTO = checkingService.createBooking(walkInRequestDTO, userId);

            response.put("status", "Booking created successfully.");
            response.put("booking", bookingResponseDTO);

            return ResponseEntity.ok(response);
        } catch (RoomNotAvailableException | CustomerNotFoundException | IllegalArgumentException e) {
            response.put("status", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "An unexpected error occurred.");
            response.put("errorMessage", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * API endpoint to retrieve booking details by ID.
     *
     * @param id ID of the booking to retrieve.
     * @return 200 OK with booking details if found, or 404 Not Found if not.
     */
    @GetMapping("/reservations/{id}")
    public ResponseEntity<BookingResponseDTO> getBooking(@PathVariable Long id) {
        BookingResponseDTO bookingResponse = bookingService.getBookingResponse(id);

        if (bookingResponse != null) {
            return ResponseEntity.ok(bookingResponse);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * API endpoint to check if a booking is considered late for check-in.
     *
     * @param id ID of the booking.
     * @return 200 OK with late check-in status, or 404 Not Found if booking doesn't exist.
     */
    @GetMapping("/reservations/{id}/late-checkin-status")
    public ResponseEntity<?> getLateCheckinStatus(@PathVariable Long id) {
        LateCheckinStatusDTO statusDTO = bookingService.getLateCheckinStatus(id);

        if (statusDTO == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found.");
        }

        return ResponseEntity.ok(statusDTO);
    }

    /**
     * API endpoint to perform check-in for guests with existing reservations.
     *
     * <p>
     * Updates room statuses from "reserved" to "occupied".
     * </p>
     *
     * <p>
     * Response:
     * <ul>
     *     <li>200 OK if check-in was successful (either fully or partially).</li>
     *     <li>400 Bad Request if booking ID is not found.</li>
     *     <li>500 Internal Server Error for unexpected errors.</li>
     * </ul>
     * </p>
     *
     * @param request Object containing booking ID and list of room IDs to check-in.
     * @return Response with successfully checked-in rooms and failed ones if any.
     */
    @PostMapping("/checkins")
    public ResponseEntity<?> checkinRooms(@RequestBody CheckinRequestDTO request) {
        try {
            CheckinResponseDTO response = bookingService.checkinRooms(request);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            // Trả về ErrorResponse chi tiết với message và lỗi
            ErrorResponse errorResponse = new ErrorResponse(
                    ex.getReason(),
                    Map.of("bookingId", "Không tìm thấy booking với ID: " + request.getBookingId())
            );
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(errorResponse);
        } catch (Exception ex) {
            // Trả về lỗi hệ thống chung
            ErrorResponse errorResponse = new ErrorResponse("Lỗi hệ thống", Map.of("detail", ex.getMessage()));
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * API endpoint to cancel check-in for guests with existing reservations.
     *
     * <p>
     * Updates room statuses from "occupied" or "reserved" to "AVAILABLE".
     * </p>
     *
     * <p>
     * Response:
     * <ul>
     *     <li>200 OK if cancellation was successful (either fully or partially).</li>
     *     <li>400 Bad Request if booking ID is not found.</li>
     *     <li>500 Internal Server Error for unexpected errors.</li>
     * </ul>
     * </p>
     *
     * @param request Object containing booking ID and list of room IDs to cancel.
     * @return Response with successfully cancelled rooms and failed ones if any.
     */
    @PostMapping("/checkins/cancelled")
    public ResponseEntity<?> cancelCheckinRooms(@RequestBody CheckinRequestDTO request) {
        try {
            // Assuming a similar response DTO or a generic one can be used.
            // This service method will need to be created in BookingServiceImpl
            CheckinResponseDTO response = bookingService.cancelRooms(request);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            ErrorResponse errorResponse = new ErrorResponse(
                    ex.getReason(),
                    Map.of("bookingId", "Không tìm thấy booking với ID: " + request.getBookingId())
            );
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .body(errorResponse);
        } catch (Exception ex) {
            ErrorResponse errorResponse = new ErrorResponse("Lỗi hệ thống", Map.of("detail", ex.getMessage()));
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @GetMapping("/checkins/{id}/receipt")
    public ResponseEntity<BookingResponseDTO> getReceipt(@PathVariable Long id) {
        BookingResponseDTO bookingResponse = bookingService.getBookingResponse(id);

        if (bookingResponse != null) {
            return ResponseEntity.ok(bookingResponse);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}

