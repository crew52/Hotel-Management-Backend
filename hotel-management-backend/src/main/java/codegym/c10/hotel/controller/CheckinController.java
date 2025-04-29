package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.BookingResponseDTO;
import codegym.c10.hotel.dto.WalkInRequestDTO;
import codegym.c10.hotel.entity.Booking;
import codegym.c10.hotel.exception.CustomerNotFoundException;
import codegym.c10.hotel.exception.RoomNotAvailableException;
import codegym.c10.hotel.service.AuthenticatedUserService;
//import codegym.c10.hotel.service.checkin.CheckingService;
import codegym.c10.hotel.service.booking.BookingServiceImpl;
import codegym.c10.hotel.service.checkin.CheckingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
     * API endpoint để tạo mới booking walk-in (đặt phòng trực tiếp tại quầy).
     *
     * <p>Quy trình:
     * <ul>
     *     <li>Trích xuất userId từ request.</li>
     *     <li>Gọi service {@link CheckingService#createBooking(WalkInRequestDTO, Long)} để tạo booking mới.</li>
     *     <li>Trả về thông tin booking đã tạo thành công.</li>
     * </ul>
     * </p>
     *
     * <p>Xử lý lỗi:
     * <ul>
     *     <li>Trả về mã HTTP 400 (Bad Request) nếu xảy ra các lỗi như: phòng không khả dụng, khách hàng không tồn tại, hoặc dữ liệu đầu vào không hợp lệ.</li>
     *     <li>Trả về mã HTTP 500 (Internal Server Error) nếu xảy ra lỗi bất ngờ trong hệ thống.</li>
     * </ul>
     * </p>
     *
     * @param walkInRequestDTO Dữ liệu booking gửi từ client (bao gồm thông tin khách hàng, danh sách phòng, thời gian nhận phòng, v.v...).
     * @param request Đối tượng {@link HttpServletRequest} chứa thông tin xác thực người dùng.
     * @return {@link ResponseEntity} chứa trạng thái booking và dữ liệu booking chi tiết nếu thành công;
     *         hoặc thông tin lỗi nếu thất bại.
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

    @GetMapping("/reservations/{id}")
    public ResponseEntity<BookingResponseDTO> getBooking(@PathVariable Long id) {
        BookingResponseDTO bookingResponse = bookingService.getBookingResponse(id);

        if (bookingResponse != null) {
            return ResponseEntity.ok(bookingResponse);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

