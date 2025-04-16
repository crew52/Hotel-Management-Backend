// hotel/dto/permission/PermissionDto.java
package codegym.c10.hotel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDto {
    private Long id;

    @NotBlank(message = "Permission name must not be blank")
    @Size(max = 100, message = "Permission name must be less than or equal to 100 characters")
    private String name;

    // Có thể thêm các trường khác nếu cần, ví dụ: description
}