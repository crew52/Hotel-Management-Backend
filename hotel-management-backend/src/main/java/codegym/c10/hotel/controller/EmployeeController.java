package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.ApiResponse;
import codegym.c10.hotel.dto.EmployeeDto;
import codegym.c10.hotel.entity.Employee;
import codegym.c10.hotel.entity.User;

import codegym.c10.hotel.service.employees.IEmployeeService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin("*")
@RequiredArgsConstructor
public class EmployeeController {
    
    private final IEmployeeService employeeService;
    
    @GetMapping
    public ResponseEntity<Page<Employee>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Employee> employees = employeeService.findAllByDeletedFalse(pageable);
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        try {
            Employee employee = employeeService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));
            return ResponseEntity.ok(employee);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getEmployeeByUserId(@PathVariable Long userId) {
        try {
            Employee employee = employeeService.findByUserId(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with user id: " + userId));
            return ResponseEntity.ok(employee);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createEmployee(@Valid @RequestBody EmployeeDto employeeDto) {
        try {
            // Validate unique constraints
            if (employeeDto.getPhone() != null && employeeService.existsByPhone(employeeDto.getPhone())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Phone number already exists"));
            }
            
            if (employeeDto.getIdCard() != null && employeeService.existsByIdCard(employeeDto.getIdCard())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "ID card already exists"));
            }
            
            // Convert DTO to entity
            Employee employee = convertToEntity(employeeDto);
            
            // Save entity
            Employee savedEmployee = employeeService.save(employee);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Employee created successfully", savedEmployee));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error creating employee: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDto employeeDto) {
        
        try {
            // Validate if employee exists
            employeeService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));
            
            // Validate unique constraints
            if (employeeDto.getPhone() != null && 
                employeeService.existsByPhoneAndIdNot(employeeDto.getPhone(), id)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Phone number already exists"));
            }
            
            if (employeeDto.getIdCard() != null && 
                employeeService.existsByIdCardAndIdNot(employeeDto.getIdCard(), id)) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "ID card already exists"));
            }
            
            // Convert DTO to entity
            Employee employee = convertToEntity(employeeDto);
            employee.setId(id);
            
            // Update entity
            Employee updatedEmployee = employeeService.update(employee);
            
            return ResponseEntity.ok(new ApiResponse(true, "Employee updated successfully", updatedEmployee));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error updating employee: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
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
    
    private Employee convertToEntity(EmployeeDto employeeDto) {
        Employee employee = new Employee();
        
        // Set user
        User user = new User();
        user.setId(employeeDto.getUserId());
        employee.setUser(user);
        
        // Set other fields
        employee.setFullName(employeeDto.getFullName());
        employee.setGender(employeeDto.getGender());
        employee.setDob(employeeDto.getDob());
        employee.setPhone(employeeDto.getPhone());
        employee.setIdCard(employeeDto.getIdCard());
        employee.setAddress(employeeDto.getAddress());
        employee.setPosition(employeeDto.getPosition());
        employee.setDepartment(employeeDto.getDepartment());
        employee.setStartDate(employeeDto.getStartDate());
        employee.setNote(employeeDto.getNote());
        employee.setImgUrl(employeeDto.getImgUrl());
        
        return employee;
    }
}