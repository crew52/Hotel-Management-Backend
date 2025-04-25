package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.BookingResponseDTO;
import codegym.c10.hotel.dto.WalkInRequestDTO;
import codegym.c10.hotel.exception.RoomNotAvailableException;
import codegym.c10.hotel.service.AuthenticatedUserService;
import codegym.c10.hotel.service.checkin.CheckingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
public class CheckinController {
    @Autowired
    private CheckingService checkingService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @PostMapping("/walkin")
    public ResponseEntity<Map<String, Object>> checkinWalking(@RequestBody WalkInRequestDTO walkInRequestDTO, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long userId = authenticatedUserService.extractUserId(request);

            // Gọi service để tạo booking, đồng thời kiểm tra tình trạng phòng.
            BookingResponseDTO bookingResponseDTO = checkingService.createBooking(walkInRequestDTO, userId);

            // Trả về thông tin booking và phòng đã được đặt.
            response.put("status", "Booking created successfully.");
            response.put("booking", bookingResponseDTO); // Trả về chi tiết booking

            return ResponseEntity.ok(response);
        } catch (RoomNotAvailableException e) {
            response.put("status", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            // Log lỗi chi tiết hơn để xác định nguyên nhân
            e.printStackTrace();  // Hoặc bạn có thể sử dụng logger để ghi log
            response.put("status", "An unexpected error occurred.");
            response.put("errorMessage", e.getMessage());  // Thêm chi tiết thông báo lỗi
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}

