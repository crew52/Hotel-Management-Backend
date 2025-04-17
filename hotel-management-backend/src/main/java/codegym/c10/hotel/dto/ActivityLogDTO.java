package codegym.c10.hotel.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLogDTO {
    private Long id;
    private String fullName;       // tên nhân viên
    private Long userId;
    private String username;       // tên tài khoản
    private String action;

    @JsonIgnore
    private LocalDateTime timestamp;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    public LocalDate getDate() {
        return timestamp != null ? timestamp.toLocalDate() : null;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    public LocalTime getTime() {
        return timestamp != null ? timestamp.toLocalTime() : null;
    }
    private String description;
}

