package codegym.c10.hotel.validator;

import codegym.c10.hotel.dto.auth.ChangePassDto;
import codegym.c10.hotel.validator.OldPasswordMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;


public class OldPasswordMatchValidator implements ConstraintValidator<OldPasswordMatch, ChangePassDto> {

    private static final Logger logger = LoggerFactory.getLogger(OldPasswordMatchValidator.class); // Thêm logger

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean isValid(ChangePassDto changePassDto, ConstraintValidatorContext context) {
        if (changePassDto == null || changePassDto.getOldPassword() == null) { // Kiểm tra null cẩn thận hơn
            return true; // Hoặc false nếu bắt buộc phải có
        }

        String oldPassword = changePassDto.getOldPassword();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("User not authenticated during OldPasswordMatch validation.");
            return false;
        }

        Object principal = authentication.getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        logger.info("Starting OldPasswordMatch validation for user: {}", username);

        try { // *** BẮT ĐẦU TRY-CATCH ***
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            logger.debug("UserDetails loaded successfully for {}: {}", username, (userDetails != null));

            if (userDetails == null) {
                logger.warn("UserDetails is NULL for username: {}", username);
                // Không cần thiết lập constraint violation ở đây vì isValid trả về false là đủ
                return false;
            }

            String currentPassword = userDetails.getPassword();
            boolean matches = passwordEncoder.matches(oldPassword, currentPassword);
            logger.debug("Password match result for user {}: {}", username, matches);

            if (!matches) {
                logger.warn("Old password mismatch for user: {}", username);
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Mật khẩu hiện tại không đúng")
                        .addPropertyNode("oldPassword")
                        .addConstraintViolation();
                return false; // Validation thất bại
            }

            logger.info("OldPasswordMatch validation successful for user: {}", username);
            return true; // Validation thành công

        } catch (Exception e) { // *** BẮT LỖI BẤT NGỜ ***
            logger.error("UNEXPECTED EXCEPTION during OldPasswordMatch validation for user: {}", username, e);

            // Quyết định xử lý: Coi lỗi bất ngờ là validation thất bại
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Lỗi hệ thống khi kiểm tra mật khẩu.") // Thông báo lỗi chung chung hơn
                    // Có thể không cần addPropertyNode hoặc trỏ vào một node khác
                    .addConstraintViolation();
            return false; // Trả về false khi có lỗi không mong muốn
        } // *** KẾT THÚC TRY-CATCH ***
    }
}
