package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.ApiResponse;
import codegym.c10.hotel.dto.auth.ChangePassDto;
import codegym.c10.hotel.dto.auth.LoginRequest;
import codegym.c10.hotel.dto.auth.SignupRequest;
import codegym.c10.hotel.service.user.IUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auth") // Đường dẫn chung cho các API xác thực
public class AuthController {

    @Autowired
    private IUserService userService;

    /**
     * Endpoint xử lý yêu cầu đăng nhập.
     * @param loginRequest Chứa username/email và password.
     * @return ResponseEntity chứa ApiResponse (thành công/thất bại, thông báo, và token nếu thành công).
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        ApiResponse response = userService.loginUser(loginRequest);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            // Có thể trả về BAD_REQUEST hoặc UNAUTHORIZED tùy logic mong muốn
            // Hiện tại trả về OK nhưng success=false theo logic của UserService
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Endpoint xử lý yêu cầu đăng ký người dùng mới.
     * @param signUpRequest Chứa thông tin đăng ký (username, email, password, confirmPassword, roles).
     * Validation (@Valid) sẽ kiểm tra cả @PasswordMatch.
     * @return ResponseEntity chứa ApiResponse (thành công/thất bại và thông báo).
     */
    @PostMapping("/register") // Hoặc bạn có thể dùng /signup
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        ApiResponse response = userService.registerUser(signUpRequest);
         if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
             // Thường trả về BAD_REQUEST nếu đăng ký không thành công do dữ liệu không hợp lệ
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Endpoint xử lý yêu cầu đăng xuất.
     * Đăng xuất bằng cách xóa thông tin xác thực trong SecurityContext.
     * Client cần xóa token ở phía client (localStorage, cookie, v.v.)
     * @return ResponseEntity chứa ApiResponse thông báo đăng xuất thành công.
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> logout() {
        // Sử dụng service để xử lý logic đăng xuất
        ApiResponse response = userService.logoutUser();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse> changePassword(@Valid @RequestBody ChangePassDto changePassDto) {
        // Lấy username của người dùng đang đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        ApiResponse response = userService.changePassword(
                username,
                changePassDto.getOldPassword(),
                changePassDto.getNewPassword()
        );

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}