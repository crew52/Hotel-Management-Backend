package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.ApiResponse;
import codegym.c10.hotel.dto.UserStatusDto;
import codegym.c10.hotel.entity.User;
import codegym.c10.hotel.service.user.IUserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * API để khoá/mở khoá tài khoản người dùng
     * @param id ID của người dùng cần cập nhật trạng thái
     * @param statusDto Đối tượng chứa trạng thái khoá mới (true/false)
     * @return Thông báo kết quả cập nhật
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UserStatusDto statusDto) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
            
            // Cập nhật trạng thái khoá
            user.setIsLocked(statusDto.isLocked());
            userService.update(user);
            
            String message = statusDto.isLocked() ? 
                    "User account has been locked successfully" : 
                    "User account has been unlocked successfully";
            
            return ResponseEntity.ok(new ApiResponse(true, message));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error updating user status: " + e.getMessage()));
        }
    }
} 