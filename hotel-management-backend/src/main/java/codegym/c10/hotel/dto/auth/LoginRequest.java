package codegym.c10.hotel.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequest {
    private String usernameOrEmail;
    private String password;
}
