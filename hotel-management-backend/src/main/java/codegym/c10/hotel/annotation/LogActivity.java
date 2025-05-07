package codegym.c10.hotel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để đánh dấu các phương thức cần ghi log hoạt động.
 * Được sử dụng kết hợp với ActivityLoggingAspect để tự động ghi log.
 * 
 * Sử dụng:
 * 
 * 1. Chỉ áp dụng cho các phương thức quan trọng cần theo dõi
 * 2. Đặt trên phương thức interface hoặc triển khai
 * 3. Cung cấp mã hành động có ý nghĩa (VD: THEM_PHONG, XOA_DAT_PHONG)
 * 4. Có thể thêm mô tả chi tiết (không bắt buộc)
 * 
 * Ví dụ:
 * {@code @LogActivity(action = "TAO_HOA_DON", description = "Tạo hóa đơn cho khách hàng")}
 */
@Target(ElementType.METHOD) // Chỉ áp dụng cho phương thức
@Retention(RetentionPolicy.RUNTIME) // Tồn tại lúc runtime để AOP đọc được
public @interface LogActivity {
    /**
     * Tên của hành động được ghi log (ví dụ: LOGIN, CREATE_INVOICE, UPDATE_ROOM)
     */
    String action();

    /**
     * Mô tả tùy chọn về hành động
     */
    String description() default "";
} 