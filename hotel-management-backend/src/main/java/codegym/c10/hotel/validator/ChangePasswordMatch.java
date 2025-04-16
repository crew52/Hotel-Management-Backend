package codegym.c10.hotel.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ChangePasswordMatchValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChangePasswordMatch {
    String message() default "Mật khẩu không hợp lệ"; // Thông báo lỗi chung
    String passwordMismatchMessage() default "Mật khẩu và xác nhận mật khẩu không khớp";
    String newPasswordSameAsOldMessage() default "Mật khẩu mới đang bị trùng với mật khẩu cũ vui lòng nhập lại";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}