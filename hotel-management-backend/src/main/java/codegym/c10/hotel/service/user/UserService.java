package codegym.c10.hotel.service.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import codegym.c10.hotel.dto.ApiResponse;
import codegym.c10.hotel.dto.auth.LoginRequest;
import codegym.c10.hotel.dto.auth.SignupRequest;
import codegym.c10.hotel.dto.auth.UserPrinciple;
import codegym.c10.hotel.entity.Role;
import codegym.c10.hotel.entity.User;
import codegym.c10.hotel.repository.RoleRepository;
import codegym.c10.hotel.repository.UserRepository;
import codegym.c10.hotel.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final RoleRepository roleRepository; // Sử dụng final và constructor injection
    private final UserRepository userRepository; // Sử dụng final và constructor injection
    private final JwtUtil jwtUtil;             // Sử dụng final và constructor injection
    private final PasswordEncoder passwordEncoder; // Sử dụng final và constructor injection

    @Autowired // Có thể cần hoặc không tùy phiên bản Spring, nhưng nên có để rõ ràng
    public UserService(RoleRepository roleRepository,
                       UserRepository userRepository,
                       JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ApiResponse registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return new ApiResponse(false, "Username already exists");
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setPasswordHash(this.passwordEncoder.encode(signupRequest.getPassword()));

        // ---> THÊM DÒNG NÀY VÀO <---
        user.setEmail(signupRequest.getEmail());
        // -----------------------------

        // Chuyển đổi roles từ request thành các đối tượng Role đã tồn tại trong DB
        Set<Role> persistedRoles = new HashSet<>();
        // ... (phần xử lý roles giữ nguyên)
        for (Role roleRequest : signupRequest.getRoles()) {
            Role persistedRole = roleRepository.findByName(roleRequest.getName());
            if (persistedRole != null) {
                persistedRoles.add(persistedRole);
            } else {
                // Bạn có thể xử lý trường hợp role không tồn tại:
                return new ApiResponse(false, "Role " + roleRequest.getName() + " not found");
            }
        }
        user.setRoles(persistedRoles);

        userRepository.save(user); // Bây giờ user đã có email trước khi lưu
        return new ApiResponse(true, "User registered successfully");
    }

    @Override
    public ApiResponse loginUser(LoginRequest loginRequest) {
        String loginInput = loginRequest.getUsernameOrEmail();
        User user = userRepository.findByUsername(loginInput);
        if (user == null) {
            return new ApiResponse(false, "Tài khoản hoặc mật khẩu không chính xác");
        }
        if (!this.passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            return new ApiResponse(false, "Tài khoản hoặc mật khẩu không chính xác");
        }

        // Sử dụng UserPrinciple.build(user) để lấy thông tin chính xác của user bao gồm authorities
        UserDetails userDetails = UserPrinciple.build(user);

        Map<String, Object> extraClaims = new HashMap<>();

        // --- THAY ĐỔI Ở ĐÂY ---
        // Lấy danh sách tên vai trò (String) thay vì đối tượng Role
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName) // Lấy tên từ đối tượng Role
                .collect(Collectors.toSet());
        extraClaims.put("roles", roleNames); // Đưa Set<String> vào claims


        String jwtToken = this.jwtUtil.generateToken(extraClaims, userDetails);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", jwtToken);

        return new ApiResponse(true, "Login successful", responseData);
    }

    @Override
    public User getUser(String username) {
        User user = userRepository.findByUsername(username);
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // *** CỰC KỲ QUAN TRỌNG: PHẢI TRIỂN KHAI ĐÚNG PHƯƠNG THỨC NÀY ***
        User user = userRepository.findByUsername(usernameOrEmail);
        if (user == null) {
            user = userRepository.findByEmail(usernameOrEmail);
            if (user == null) {
                logger.error("User not found with username or email: {}", usernameOrEmail);
                throw new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
            }
        }
        logger.info("User found: {}", usernameOrEmail);
        return UserPrinciple.build(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return null;
    }

    @Override
    public User save(User user) {
        return null;
    }

    @Override
    public void delete(User user) {

    }
}
