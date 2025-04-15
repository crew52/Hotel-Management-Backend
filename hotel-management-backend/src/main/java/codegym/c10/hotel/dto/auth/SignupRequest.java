package codegym.c10.hotel.dto.auth;

import codegym.c10.hotel.entity.Role;
import codegym.c10.hotel.validator.PasswordMatch;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;


import java.util.Set;

@Getter @Setter
@PasswordMatch
public class SignupRequest {
    @NotBlank(message = "Tên người dùng không được để trống")
    private String username;
    @NotBlank(message = "Email không được để trống")
    private String email;
    @NotBlank(message = "Password không được để trống")
    private String password;
    @NotBlank(message = "Password xác nhận không được để trống")
    private String confirmPassword;

    private Set<Role> roles;
}
