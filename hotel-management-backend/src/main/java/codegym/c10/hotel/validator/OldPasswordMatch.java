package codegym.c10.hotel.validator;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OldPasswordMatchValidator.class)
@Target({ElementType.TYPE}) // Áp dụng ở cấp độ lớp (để truy cập nhiều trường)
@Retention(RetentionPolicy.RUNTIME)
public @interface OldPasswordMatch {
    String message() default "Mật khẩu cũ không khớp với mật khẩu hiện tại";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}