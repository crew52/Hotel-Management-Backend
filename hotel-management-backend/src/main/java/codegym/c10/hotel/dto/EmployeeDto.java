package codegym.c10.hotel.dto;

import codegym.c10.hotel.eNum.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDto {
    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Full name must not be blank")
    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullName;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10–15 digits")
    private String phone;

    @Pattern(regexp = "^[A-Z0-9]{5,20}$", message = "ID card must be 5–20 characters, letters and digits only")
    private String idCard;

    @Size(max = 255, message = "Address must be at most 255 characters")
    private String address;

    @Size(max = 100, message = "Position must be at most 100 characters")
    private String position;

    @Size(max = 100, message = "Department must be at most 100 characters")
    private String department;

    @NotNull(message = "Start date is required")
    @PastOrPresent(message = "Start date cannot be in the future")
    private LocalDate startDate;

    @Size(max = 65535, message = "Note is too long")
    private String note;

    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    private String imgUrl;

    // Thêm trường để lưu thông tin người dùng khi trả về
    private UserDto user;
}
