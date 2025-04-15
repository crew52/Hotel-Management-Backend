package codegym.c10.hotel.validator;

import codegym.c10.hotel.dto.auth.ChangePassDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ChangePasswordMatchValidator implements ConstraintValidator<ChangePasswordMatch, ChangePassDto> {
@Override
    public boolean isValid(ChangePassDto changePassDto, ConstraintValidatorContext context) {
        if (changePassDto == null) {
            return true; // Nếu object là null thì coi như valid (tùy logic, có thể false)
        }
        String newPassword = changePassDto.getNewPassword();
        String confirmNewPassword = changePassDto.getConfirmNewPassword();

        return newPassword != null && newPassword.equals(confirmNewPassword); // Kiểm tra password và confirmPassword có khớp nhau
    }
}
