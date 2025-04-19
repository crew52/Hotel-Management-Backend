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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Endpoint để lấy thông tin quyền của người dùng hiện tại.
     * Chỉ trả về thông tin quyền (permissions), không bao gồm roles.
     * 
     * @return Danh sách các quyền của người dùng hiện tại
     */
    @GetMapping("/permissions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getCurrentUserPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Lấy tất cả các quyền (bao gồm cả permissions và roles)
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // Tách riêng permissions và roles
        List<String> permissions = new ArrayList<>();
        List<String> roles = new ArrayList<>();
        
        for (GrantedAuthority authority : authorities) {
            String authorityName = authority.getAuthority();
            if (authorityName.startsWith("ROLE_")) {
                roles.add(authorityName);
            } else {
                permissions.add(authorityName);
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("permissions", permissions);
        response.put("roles", roles);
        response.put("username", authentication.getName());
        
        return ResponseEntity.ok(response);
    }
}