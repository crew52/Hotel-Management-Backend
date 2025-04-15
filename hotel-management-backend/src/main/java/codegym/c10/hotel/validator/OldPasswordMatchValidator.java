package codegym.c10.hotel.validator;

import codegym.c10.hotel.dto.auth.ChangePassDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class OldPasswordMatchValidator implements ConstraintValidator<OldPasswordMatch, ChangePassDto> {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean isValid(ChangePassDto changePassDto, ConstraintValidatorContext context) {
        if (changePassDto == null) {
            return true; // Hoặc false, tùy thuộc vào yêu cầu của bạn về sự hiện diện của DTO
        }

        String oldPassword = changePassDto.getOldPassword();

        // 1. Lấy người dùng đã xác thực hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false; // Không có người dùng đã xác thực
        }

        Object principal = authentication.getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString(); // Hoặc xử lý khác tùy thuộc vào kiểu principal của bạn
        }

        // 2. Tải thông tin chi tiết của người dùng từ dịch vụ của bạn
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (userDetails == null) {
            return false; // Không tìm thấy người dùng (không nên xảy ra nếu đã xác thực)
        }

        String currentPassword = userDetails.getPassword();

        // 3. So sánh oldPassword đã cung cấp với mật khẩu đã mã hóa hiện tại
        return passwordEncoder.matches(oldPassword, currentPassword);
    }
}