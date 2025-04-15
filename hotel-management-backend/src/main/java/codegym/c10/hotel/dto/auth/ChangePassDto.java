package codegym.c10.hotel.dto.auth;


import codegym.c10.hotel.validator.OldPasswordMatch;
import codegym.c10.hotel.validator.ChangePasswordMatch;
import lombok.Data;

@Data
@OldPasswordMatch
@ChangePasswordMatch(passwordMismatchMessage = "Mật khẩu mới và xác nhận mật khẩu không khớp.",
        newPasswordSameAsOldMessage = "Mật khẩu mới đang trùng với mật khẩu cũ.")
public class ChangePassDto {
    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;

}
