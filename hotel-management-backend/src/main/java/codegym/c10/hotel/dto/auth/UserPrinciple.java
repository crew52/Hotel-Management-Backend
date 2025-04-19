package codegym.c10.hotel.dto.auth;

import codegym.c10.hotel.entity.Permission;
import codegym.c10.hotel.entity.Role;
import codegym.c10.hotel.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects; // Import Objects for null-safe equals

public class UserPrinciple implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id; // User ID
    private final String username;
    private final String password; // Lưu password hash từ User entity
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean isLocked; // Trạng thái khóa tài khoản
    private final boolean isDeleted; // Trạng thái xóa mềm (từ BaseEntity)


    // Constructor nhận đầy đủ thông tin
    public UserPrinciple(Long id, String username, String password, Collection<? extends GrantedAuthority> authorities, boolean isLocked, boolean isDeleted) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.isLocked = isLocked;
        this.isDeleted = isDeleted;
    }

    /**
     * Phương thức factory để tạo UserPrinciple từ User entity.
     *
     * @param user User entity từ database.
     * @return Một đối tượng UserPrinciple.
     */
    public static UserPrinciple build(User user) {
        // 1. Lấy danh sách quyền từ roles
        List<GrantedAuthority> authoritiesList = new ArrayList<>();
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                // Đảm bảo role và tên role không null trước khi thêm
                if (role != null && role.getName() != null) {
                    // Thêm role name như một authority
                    authoritiesList.add(new SimpleGrantedAuthority(role.getName()));
                    
                    // Thêm tất cả permissions của role như các authorities
                    if (role.getPermissions() != null) {
                        for (Permission permission : role.getPermissions()) {
                            if (permission != null && permission.getName() != null) {
                                authoritiesList.add(new SimpleGrantedAuthority(permission.getName()));
                            }
                        }
                    }
                } else {
                    // Ghi log hoặc xử lý nếu có role bất thường (ví dụ: role null hoặc tên null trong set)
                    System.err.println("Cảnh báo: Phát hiện Role hoặc Role Name null cho User ID: " + user.getId());
                }
            }
        }

        // 2. Lấy trạng thái isLocked và isDeleted từ User entity
        // Xử lý trường hợp giá trị Boolean có thể là null từ DB
        // Mặc định: không khóa (false), không xóa (false) nếu giá trị là null
        boolean locked = user.getIsLocked() != null && user.getIsLocked();
        boolean deleted = user.getDeleted() != null && user.getDeleted(); // Giả định getDeleted() đã hoạt động

        // 3. Tạo và trả về đối tượng UserPrinciple
        return new UserPrinciple(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(), // Lấy password hash
                authoritiesList,
                locked,      // Trạng thái khóa thực tế
                deleted      // Trạng thái xóa thực tế
        );
    }

    // --- Implement UserDetails Methods ---

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // Trả về password hash đã lưu
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        // Mặc định là true, trừ khi có logic hết hạn tài khoản
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Tài khoản KHÔNG bị khóa nếu trường isLocked là false
        return !this.isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // Mặc định là true, trừ khi có logic hết hạn mật khẩu
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Tài khoản được coi là enabled (hoạt động) nếu trường deleted là false
        return !this.isDeleted;
    }

    // --- Equals & HashCode (Dựa trên ID) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrinciple that = (UserPrinciple) o;
        // Nếu ID null thì không bằng nhau, nếu không thì so sánh ID
        if (id == null) {
            return that.id == null;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        // Dùng ID để tính hashCode, nếu ID null thì dùng giá trị mặc định (ví dụ 0)
        return Objects.hash(id != null ? id : 0);
    }
}