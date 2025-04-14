package codegym.c10.hotel.validator;



import codegym.c10.hotel.dto.auth.SignupRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, SignupRequest> { // Implement ConstraintValidator

    @Override
    public boolean isValid(SignupRequest registerRequest, ConstraintValidatorContext context) {
        if (registerRequest == null) {
            return true; // Nếu object là null thì coi như valid (tùy logic, có thể false)
        }
        String password = registerRequest.getPassword();
        String confirmPassword = registerRequest.getConfirmPassword();

        return password != null && password.equals(confirmPassword); // Kiểm tra password và confirmPassword có khớp nhau
    }
}