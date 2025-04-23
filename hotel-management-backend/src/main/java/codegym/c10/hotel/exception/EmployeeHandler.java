package codegym.c10.hotel.exception;

import codegym.c10.hotel.dto.EmployeeDto;
import codegym.c10.hotel.service.employees.IEmployeeService;
import codegym.c10.hotel.service.uploadFile.StorageService;
import jakarta.validation.ConstraintViolation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import jakarta.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class EmployeeHandler {

    @Autowired
    private IEmployeeService employeeService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private Validator validator;

    public ResponseEntity<?> createEmployeeDto(EmployeeDto employeeDto, BindingResult bindingResult, MultipartFile img) throws IOException {
        // Validate RoomCategory object for required fields
        Map<String, String> errors = validate(employeeDto, bindingResult, true);

        // Validate based on annotation-based constraints (e.g., @NotNull, @Size)
        validateAnnotations(employeeDto, errors);

        if (!errors.isEmpty()) {
            // If validation errors exist, return a bad request response
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed", errors));
        }


        // Save the new RoomCategory and return a response with the saved category
        EmployeeDto employee = employeeService.createEmployeeWithImage(employeeDto, img);
        return new ResponseEntity<>(employee, HttpStatus.CREATED); // HTTP status 201 (Created)
    }

    private Map<String, String> validate(EmployeeDto employeeDto, BindingResult bindingResult, boolean isCreate) {
        return validate(employeeDto, bindingResult, isCreate, null);
    }

    private Map<String, String> validate(EmployeeDto employeeDto, BindingResult bindingResult, boolean isCreate, Long id) {
        Map<String, String> errors = new HashMap<>();

        if (bindingResult.hasErrors()) {
            // Collect any field validation errors
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
        }

        // Kiểm tra trùng lặp ID Card
        boolean idCardExists = isCreate ?
                employeeService.existsByIdCard(employeeDto.getIdCard()) :
                employeeService.existsByIdCardAndIdNot(employeeDto.getIdCard(), id);

        if (idCardExists) {
            errors.put("idCard", "ID Card already exists");
        }

        // Kiểm tra trùng lặp số điện thoại
        boolean phoneExists = isCreate ?
                employeeService.existsByPhone(employeeDto.getPhone()) :
                employeeService.existsByPhoneAndIdNot(employeeDto.getPhone(), id);

        if (phoneExists) {
            errors.put("phone", "Phone number already exists");
        }

        // Kiểm tra trùng lặp userId
        if (employeeDto.getUserId() != null) {
            boolean userIdExists = isCreate ?
                    employeeService.existsByUserId(employeeDto.getUserId()) :
                    employeeService.existsByUserIdAndIdNot(employeeDto.getUserId(), id);

            if (userIdExists) {
                errors.put("userId", "User is already assigned to another employee");
            }
        }

        return errors;
    }

    private void validateAnnotations(EmployeeDto employeeDto, Map<String, String> errors) {
        Set<ConstraintViolation<EmployeeDto>> violations = validator.validate(employeeDto);
        for (ConstraintViolation<EmployeeDto> violation : violations) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
    }
}