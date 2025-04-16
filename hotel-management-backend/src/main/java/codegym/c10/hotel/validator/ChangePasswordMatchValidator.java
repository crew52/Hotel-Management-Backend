package codegym.c10.hotel.validator;

import codegym.c10.hotel.dto.auth.ChangePassDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ChangePasswordMatchValidator implements ConstraintValidator<ChangePasswordMatch, ChangePassDto> {

    private String passwordMismatchMessage;
    private String newPasswordSameAsOldMessage;

    @Override
    public void initialize(ChangePasswordMatch constraintAnnotation) {
        this.passwordMismatchMessage = constraintAnnotation.passwordMismatchMessage();
        this.newPasswordSameAsOldMessage = constraintAnnotation.newPasswordSameAsOldMessage();
    }

    @Override
    public boolean isValid(ChangePassDto changePassDto, ConstraintValidatorContext context) {
        if (changePassDto == null) {
            return true; // Nếu object là null thì coi như valid (tùy logic, có thể false)
        }
        String newPassword = changePassDto.getNewPassword();
        String confirmNewPassword = changePassDto.getConfirmNewPassword();
        String oldPassword = changePassDto.getOldPassword();

        boolean isValid = true;

        if (newPassword != null && !newPassword.equals(confirmNewPassword)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(passwordMismatchMessage)
                    .addPropertyNode("confirmNewPassword") // Chỉ định trường gây ra lỗi
                    .addConstraintViolation();
            isValid = false;
        }

        if (newPassword != null && oldPassword != null && newPassword.equals(oldPassword)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(newPasswordSameAsOldMessage)
                    .addPropertyNode("newPassword") // Chỉ định trường gây ra lỗi
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}