package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.ApiResponse;
import codegym.c10.hotel.service.AuthenticatedUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/userId")
public class UserInfoController {

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    // GET userId từ token
    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserId(HttpServletRequest request) {
        try {
            Long userId = authenticatedUserService.extractUserId(request); // Gọn gàng
            return ResponseEntity.ok(Map.of("userId", userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error while extracting user ID"));
        }
    }

    // Một GET khác (ví dụ: dùng userId để xử lý booking)
    @GetMapping("/info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        try {
            Long userId = authenticatedUserService.extractUserId(request); // tái sử dụng
            // Dùng userId để lấy thông tin hoặc xử lý khác
            return ResponseEntity.ok(Map.of("userId", userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error while retrieving user info"));
        }
    }
}