package codegym.c10.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO chứa thông tin trạng thái khoá của người dùng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDto {
    private boolean locked;
} 