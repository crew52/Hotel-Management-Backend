package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.ApiResponse;
import codegym.c10.hotel.dto.EmployeeDto;
import codegym.c10.hotel.exception.ErrorResponse;
import codegym.c10.hotel.service.employees.IEmployeeService;
import codegym.c10.hotel.service.user.IUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import codegym.c10.hotel.exception.EmployeeHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin("*")
@RequiredArgsConstructor
public class EmployeeController {
    private final IEmployeeService employeeService;

    @Autowired
    private IUserService userService;

    @Autowired
    private EmployeeHandler employeeHandler;


    @GetMapping
    @PreAuthorize("@securityService.hasPermission('VIEW_EMPLOYEE')")
    public ResponseEntity<Page<EmployeeDto>> getAllEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<EmployeeDto> employees = employeeService.findEmployeesByFilters(
                department, position, pageable);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.hasPermission('VIEW_EMPLOYEE')")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        try {
            EmployeeDto employeeDto = employeeService.findEmployeeDtoById(id);
            return ResponseEntity.ok(employeeDto);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@securityService.hasPermission('DELETE_EMPLOYEE')")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try {
            employeeService.remove(id);
            return ResponseEntity.ok(new ApiResponse(true, "Employee deleted successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error deleting employee: " + e.getMessage()));
        }
    }

    private EmployeeDto parseEmployeeDtoJson(String employeeDtoJson) {
        try {
            ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
            return mapper.readValue(employeeDtoJson, EmployeeDto.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @PutMapping(value = "/{id}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('UPDATE_EMPLOYEE')")
    public ResponseEntity<?> updateEmployeeWithImage(
            @PathVariable Long id,
            @RequestPart("employee") String employeeJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {

        try {
            EmployeeDto employeeDto = parseEmployeeDtoJson(employeeJson);
            if (employeeDto == null) {
                Map<String, String> error = new HashMap<>();
                error.put("employee", "Invalid JSON format or value");
                return ResponseEntity.badRequest().body(new ErrorResponse("Invalid employee data", error));
            }

            // ID được truyền từ PathVariable sẽ overwrite mọi giá trị trong employeeDto
            EmployeeDto updatedEmployeeDto = employeeService.updateEmployeeWithImage(id, employeeDto, imageFile);

            return ResponseEntity.ok(new ApiResponse(true, "Employee updated successfully", updatedEmployeeDto));

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error updating employee: " + e.getMessage()));
        }
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('CREATE_EMPLOYEE')")
    public ResponseEntity<?> createEmployee(@Valid @RequestPart("employee") String employeeJson,
                                            BindingResult bindingResult,
                                            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {
        EmployeeDto employeeDto = parseEmployeeDtoJson(employeeJson);

        if (employeeDto == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid value provided",
                    Collections.singletonMap("employee", "Invalid JSON format or value")));
        }

        // Kiểm tra userId có tồn tại không (có thể giữ lại hoặc chuyển logic này sang handler nếu muốn gom toàn bộ validation)
        if (employeeDto.getUserId() == null || !userService.existsById(employeeDto.getUserId())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("User not found",
                    Collections.singletonMap("user", "User does not exist")));
        }

        // Gọi handler để xử lý
        return employeeHandler.createEmployeeDto(employeeDto, bindingResult, imageFile);
    }

}