package codegym.c10.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter // Sử dụng Lombok để tự động tạo getter và setter
@AllArgsConstructor // Sử dụng Lombok để tự động tạo constructor có tất cả các tham số
@NoArgsConstructor
public class ApiResponse {
    private boolean success; // Trường boolean cho biết thao tác thành công hay thất bại
    private String message;
    private Object data;
    // Trường String để chứa thông báo chi tiết (thành công hoặc lỗi)
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

}