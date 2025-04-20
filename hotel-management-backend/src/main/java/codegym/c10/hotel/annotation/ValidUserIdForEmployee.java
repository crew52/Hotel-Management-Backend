package codegym.c10.hotel.annotation;

import codegym.c10.hotel.validator.UserIdValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a User ID exists and is not already linked to another Employee.
 * This annotation can be applied to Long fields representing User IDs.
 */
@Documented
@Constraint(validatedBy = UserIdValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUserIdForEmployee {
    String message() default "Invalid user ID: User does not exist or is already linked to another employee";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
} 