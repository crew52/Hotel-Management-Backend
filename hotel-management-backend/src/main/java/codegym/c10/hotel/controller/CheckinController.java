package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.BookingResponseDTO;
import codegym.c10.hotel.dto.WalkInRequestDTO;
import codegym.c10.hotel.exception.RoomNotAvailableException;
import codegym.c10.hotel.service.AuthenticatedUserService;
//import codegym.c10.hotel.service.checkin.CheckingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
//    @Autowired
//    private CheckingService checkingService;
//
//    @Autowired
//    private AuthenticatedUserService authenticatedUserService;
//
//    @PostMapping("/walkin")
//    public ResponseEntity<Map<String, Object>> checkinWalking(
//            @Valid @RequestBody WalkInRequestDTO walkInRequestDTO,
//            HttpServletRequest request) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            Long userId = authenticatedUserService.extractUserId(request);
//
//            BookingResponseDTO bookingResponseDTO = checkingService.createBooking(walkInRequestDTO, userId);
//
//            response.put("status", "Booking created successfully.");
//            response.put("booking", bookingResponseDTO);
//
//            return ResponseEntity.ok(response);
//        } catch (RoomNotAvailableException e) {
//            response.put("status", e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.put("status", "An unexpected error occurred.");
//            response.put("errorMessage", e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }


}

