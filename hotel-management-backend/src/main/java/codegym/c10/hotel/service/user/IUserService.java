package codegym.c10.hotel.service.user;


import codegym.c10.hotel.dto.ApiResponse;
import codegym.c10.hotel.dto.auth.LoginRequest;
import codegym.c10.hotel.dto.auth.SignupRequest;
import codegym.c10.hotel.entity.User;
import codegym.c10.hotel.service.IGenerateService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface IUserService extends IGenerateService<User>, UserDetailsService {
    ApiResponse registerUser(SignupRequest signupRequest);
    ApiResponse loginUser(LoginRequest loginRequest);
    ApiResponse logoutUser();
    User getUser(String username);
    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    void delete(User user);

    ApiResponse changePassword(String username, String oldPassword, String newPassword);
}
