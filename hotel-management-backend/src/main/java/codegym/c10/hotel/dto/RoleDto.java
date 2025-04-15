// hotel/dto/role/RoleDto.java
package codegym.c10.hotel.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
    private Long id;

    @NotBlank(message = "Role name must not be blank")
    @Size(max = 100, message = "Role name must be less than or equal to 100 characters")
    private String name; // Ví dụ: "ADMIN", "USER", không cần tiền tố "ROLE_" ở đây

    // Khi tạo/cập nhật Role, có thể chỉ cần ID của Permission
    private Set<Long> permissionIds;

    // Khi lấy thông tin Role, có thể trả về chi tiết Permission
    private Set<PermissionDto> permissions;

    // Constructor chỉ nhận ID và Name (dùng khi tạo)
    public RoleDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }
     // Constructor chỉ nhận ID, Name và permissionIds (dùng khi tạo/update)
     public RoleDto(Long id, String name, Set<Long> permissionIds) {
        this.id = id;
        this.name = name;
        this.permissionIds = permissionIds;
    }
}