package codegym.c10.hotel.validator;

import codegym.c10.hotel.annotation.ValidUserIdForEmployee;
import codegym.c10.hotel.dto.EmployeeDto;
import codegym.c10.hotel.entity.Employee;
import codegym.c10.hotel.repository.EmployeeRepository;
import codegym.c10.hotel.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

/**
 * Validator for the @ValidUserIdForEmployee annotation.
 * Validates that a User ID exists and is not already linked to another Employee.
 */
public class UserIdValidator implements ConstraintValidator<ValidUserIdForEmployee, Long> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public void initialize(ValidUserIdForEmployee constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Long userId, ConstraintValidatorContext context) {
        // If value is null, let @NotNull handle it if necessary
        if (userId == null) {
            return true;
        }

        // 1. Check if user exists
        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            customMessageForValidation(context, "User with ID " + userId + " does not exist");
            return false;
        }

        // 2. Check if the user is already linked to another employee
        Optional<Employee> existingEmployee = employeeRepository.findByUserId(userId);
        
        // Nếu user không liên kết với nhân viên nào -> hợp lệ
        if (existingEmployee.isEmpty()) {
            return true;
        }
        
        // Lấy ID của nhân viên đang được cập nhật (nếu có)
        Long currentEmployeeId = getCurrentEmployeeIdFromRequest();
        
        // Nếu đang update và user này đã liên kết với chính nhân viên đang cập nhật -> hợp lệ
        if (currentEmployeeId != null && existingEmployee.get().getId().equals(currentEmployeeId)) {
            return true;
        }
        
        // Không hợp lệ: user đã liên kết với nhân viên khác
        customMessageForValidation(context, "User is already linked to another employee");
        return false;
    }
    
    /**
     * Cố gắng lấy employee ID từ đường dẫn URL trong request.
     * Vì đây là validator nên chỉ có thể truy cập thông tin từ request context.
     */
    private Long getCurrentEmployeeIdFromRequest() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr != null) {
                HttpServletRequest request = attr.getRequest();
                
                // Kiểm tra xem đường dẫn có phải là cập nhật không
                String requestURI = request.getRequestURI();
                if (requestURI != null && requestURI.matches(".*/api/employees/\\d+$") && "PUT".equals(request.getMethod())) {
                    // Lấy employee ID từ URL
                    @SuppressWarnings("unchecked")
                    Map<String, String> pathVariables = 
                            (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
                    
                    if (pathVariables != null && pathVariables.containsKey("id")) {
                        return Long.valueOf(pathVariables.get("id"));
                    }
                }
            }
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu có
            return null;
        }
        
        return null;
    }

    private void customMessageForValidation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
} 