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
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            return new ApiResponse(false, "Tài khoản hoặc mật khẩu không chính xác");
        }
        if (!this.passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            return new ApiResponse(false, "Tài khoản hoặc mật khẩu không chính xác");
        }

        // Sử dụng UserPrinciple.build(user) để lấy thông tin chính xác của user bao gồm authorities
        UserDetails userDetails = UserPrinciple.build(user);

        Map<String, Object> extraClaims = new HashMap<>();


        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName) // Lấy tên vai trò, ví dụ: "ROLE_ADMIN", "ROLE_USER"
                .collect(Collectors.toList()); // Thu thập thành List<String>
        extraClaims.put("roles", roleNames); // Đưa danh sách tên vai trò vào claims


       extraClaims.put("userId", user.getId());


        String jwtToken = this.jwtUtil.generateToken(extraClaims, userDetails);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", jwtToken);
        // Có thể thêm thông tin user vào response nếu cần thiết cho frontend
         responseData.put("userInfo", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "roles", roleNames // Gửi lại roles trong response nếu cần
         ));

        return new ApiResponse(true, "Login successful", responseData);
    }

    @Override
    public ApiResponse logoutUser() {
        logger.info("Xử lý đăng xuất người dùng");
        
        // Xóa thông tin xác thực hiện tại
        SecurityContextHolder.clearContext();
        
        // Trả về thông báo đăng xuất thành công
        return new ApiResponse(true, "Đăng xuất thành công");
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
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void remove(Long id) {

    }

    @Override
    public User save(User user) {
        // Implementation for save
        return userRepository.save(user);
    }

    @Override
    public void delete(User user) {
        // Triển khai xoá user
        user.setDeleted(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(User user) {
        // Kiểm tra user tồn tại
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + user.getId()));
        
        // Cập nhật các trường cần thiết
        existingUser.setIsLocked(user.getIsLocked());
        
        // Giữ nguyên các trường không cần thay đổi
        // Chỉ cập nhật những gì cần thiết
        
        // Lưu thông tin cập nhật
        return userRepository.save(existingUser);
    }

    @Override
    public ApiResponse changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return new ApiResponse(false, "Người dùng không tồn tại");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            return new ApiResponse(false, "Mật khẩu cũ không chính xác");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return new ApiResponse(true, "Đổi mật khẩu thành công");
    }
}
