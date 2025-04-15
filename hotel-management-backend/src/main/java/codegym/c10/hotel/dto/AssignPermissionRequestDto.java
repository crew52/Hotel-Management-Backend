// hotel/dto/role/AssignPermissionRequestDto.java (Ví dụ DTO cho việc gán/gỡ quyền)
package codegym.c10.hotel.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignPermissionRequestDto {
    @NotNull(message = "Role ID cannot be null")
    private Long roleId;

    @NotNull(message = "Permission ID cannot be null")
    private Long permissionId;
}