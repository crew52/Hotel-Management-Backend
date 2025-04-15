package codegym.c10.hotel.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordMatchValidator.class) // Liên kết annotation với class validator
@Target({ElementType.TYPE}) // Annotation này áp dụng cho TYPE (class, interface)
@Retention(RetentionPolicy.RUNTIME) // Annotation được giữ lại ở runtime
public @interface ChangePasswordMatch {
    String message() default "Mật khẩu và xác nhận mật khẩu không khớp"; // Thông báo lỗi mặc định
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}