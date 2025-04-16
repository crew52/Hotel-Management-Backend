package codegym.c10.hotel.exception;

// Các import hiện có
import io.jsonwebtoken.ExpiredJwtException;
// import jakarta.validation.ConstraintViolationException; // Bạn có thể giữ lại nếu dùng @Validated ở Service layer
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Import cần thiết cho handler mới
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

// Các import khác
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
// import com.fasterxml.jackson.databind.ObjectMapper; // Import này có thể cần nếu bạn dùng ErrorResponse cho các handler khác

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- Các handler hiện có của bạn ---

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleInvalidEnum(HttpMessageNotReadableException ex) {
        String message = "Invalid value provided";

        // Check xem có phải lỗi enum không
        Throwable mostSpecificCause = ex.getMostSpecificCause();
        if (mostSpecificCause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException) {
            com.fasterxml.jackson.databind.exc.InvalidFormatException ife =
                    (com.fasterxml.jackson.databind.exc.InvalidFormatException) mostSpecificCause;

            Class<?> targetType = ife.getTargetType();
            if (targetType.isEnum()) {
                String enumValues = Arrays.stream(targetType.getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                message = String.format("Invalid value for enum %s. Allowed values: [%s]",
                        targetType.getSimpleName(), enumValues);
            }
        }

        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return ResponseEntity.badRequest().body(error);
    }

    // Giả sử bạn có class ErrorResponse hoặc thay thế bằng Map<String, Object> nếu muốn
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Object> handleTokenExpiredException(TokenExpiredException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", "Authentication error: " + ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Object> handleExpiredJwtException(ExpiredJwtException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", "Your session has expired. Please login again.");
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", "Authentication failed: " + ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }


    // --- HANDLER MỚI CHO LỖI VALIDATION (@Valid) ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> responseBody = new HashMap<>();
        // Thông báo chung cho biết có lỗi validation
        responseBody.put("message", "Yêu cầu không hợp lệ do lỗi validation.");

        Map<String, String> fieldErrors = new HashMap<>();
        // Lấy tất cả các lỗi từ BindingResult
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String identifier;
            // Nếu lỗi là FieldError, lấy tên trường gây lỗi
            if (error instanceof FieldError) {
                identifier = ((FieldError) error).getField();
            } else {
                // Nếu là lỗi ở cấp độ đối tượng (ObjectError), lấy tên đối tượng
                identifier = error.getObjectName();
            }
            // Lấy thông báo lỗi đã định nghĩa trong annotation hoặc validator
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(identifier, errorMessage);
        });
        // Đưa danh sách lỗi chi tiết (tên trường/đối tượng -> thông báo lỗi) vào response
        responseBody.put("errors", fieldErrors);

        // Trả về mã lỗi 400 Bad Request cùng với thông tin lỗi
        return new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        Map<String, String> errors = new HashMap<>();
        // Nếu bạn biết lỗi liên quan đến trường nào, có thể đặt key tương ứng
        errors.put("notFound", ex.getMessage());

        ErrorResponse response = new ErrorResponse("Entity not found", errors);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}