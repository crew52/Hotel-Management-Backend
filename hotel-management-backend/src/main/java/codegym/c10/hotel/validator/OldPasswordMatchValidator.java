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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (userDetails == null) {
            return false;
        }

        String currentPassword = userDetails.getPassword();

        if (!passwordEncoder.matches(oldPassword, currentPassword)) {
            // Thêm một constraint violation tùy chỉnh với thông báo lỗi mong muốn
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Mật khẩu hiện tại không đúng")
                    .addPropertyNode("oldPassword") // Chỉ định trường gây ra lỗi
                    .addConstraintViolation();
            return false; // Validation thất bại
        }

        return true; // Validation thành công
    }
}