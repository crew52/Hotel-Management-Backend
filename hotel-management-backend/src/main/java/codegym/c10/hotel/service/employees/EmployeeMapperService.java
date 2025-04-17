package codegym.c10.hotel.service.employees;

import codegym.c10.hotel.dto.EmployeeDto;
import codegym.c10.hotel.dto.UserDto;
import codegym.c10.hotel.entity.Employee;
import codegym.c10.hotel.entity.Role;
import codegym.c10.hotel.entity.User;
import codegym.c10.hotel.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service chuyên biệt để chuyển đổi giữa Employee Entity và DTO
 */
@Service
@RequiredArgsConstructor
public class EmployeeMapperService {

    private final UserRepository userRepository;

    /**
     * Chuyển đổi từ Entity sang DTO
     *
     * @param employee Entity cần chuyển đổi
     * @return EmployeeDto đã chứa thông tin tương ứng
     */
    public EmployeeDto convertToDto(Employee employee) {
        if (employee == null) {
            return null;
        }

        EmployeeDto dto = new EmployeeDto();

        dto.setId(employee.getId());
        dto.setUserId(employee.getUser().getId());
        dto.setFullName(employee.getFullName());
        dto.setGender(employee.getGender());
        dto.setDob(employee.getDob());
        dto.setPhone(employee.getPhone());
        dto.setIdCard(employee.getIdCard());
        dto.setAddress(employee.getAddress());
        dto.setPosition(employee.getPosition());
        dto.setDepartment(employee.getDepartment());
        dto.setStartDate(employee.getStartDate());
        dto.setNote(employee.getNote());
        dto.setImgUrl(employee.getImgUrl());

        // Bổ sung thông tin UserDto
        User user = employee.getUser();
        if (user != null) {
            UserDto userDto = new UserDto();
            userDto.setId(user.getId());
            userDto.setUsername(user.getUsername());
            userDto.setEmail(user.getEmail());

            // Lấy tên các vai trò
            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
            userDto.setRoleNames(roleNames);

            dto.setUser(userDto);
        }

        return dto;
    }

    /**
     * Chuyển đổi từ DTO sang Entity
     *
     * @param employeeDto DTO cần chuyển đổi
     * @return Employee entity đã chứa thông tin tương ứng
     * @throws EntityNotFoundException nếu không tìm thấy User
     */
    public Employee convertToEntity(EmployeeDto employeeDto) {
        if (employeeDto == null) {
            return null;
        }

        Employee employee = new Employee();

        if (employeeDto.getId() != null) {
            employee.setId(employeeDto.getId());
        }

        // Set user
        User user = userRepository.findById(employeeDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + employeeDto.getUserId()));
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

    /**
     * Cập nhật một entity hiện có từ thông tin trong DTO
     *
     * @param existingEmployee Entity hiện có cần cập nhật
     * @param employeeDto DTO chứa thông tin mới
     * @return Employee entity đã được cập nhật
     * @throws EntityNotFoundException nếu không tìm thấy User
     */
    public Employee updateEntityFromDto(Employee existingEmployee, EmployeeDto employeeDto) {
        if (employeeDto == null || existingEmployee == null) {
            return existingEmployee;
        }

        // Chỉ cập nhật user nếu userId đã thay đổi
        if (employeeDto.getUserId() != null &&
                (existingEmployee.getUser() == null || !employeeDto.getUserId().equals(existingEmployee.getUser().getId()))) {
            User user = userRepository.findById(employeeDto.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + employeeDto.getUserId()));
            existingEmployee.setUser(user);
        }

        // Cập nhật các trường khác nếu có trong DTO
        if (employeeDto.getFullName() != null) {
            existingEmployee.setFullName(employeeDto.getFullName());
        }

        if (employeeDto.getGender() != null) {
            existingEmployee.setGender(employeeDto.getGender());
        }

        if (employeeDto.getDob() != null) {
            existingEmployee.setDob(employeeDto.getDob());
        }

        if (employeeDto.getPhone() != null) {
            existingEmployee.setPhone(employeeDto.getPhone());
        }

        if (employeeDto.getIdCard() != null) {
            existingEmployee.setIdCard(employeeDto.getIdCard());
        }

        if (employeeDto.getAddress() != null) {
            existingEmployee.setAddress(employeeDto.getAddress());
        }

        if (employeeDto.getPosition() != null) {
            existingEmployee.setPosition(employeeDto.getPosition());
        }

        if (employeeDto.getDepartment() != null) {
            existingEmployee.setDepartment(employeeDto.getDepartment());
        }

        if (employeeDto.getStartDate() != null) {
            existingEmployee.setStartDate(employeeDto.getStartDate());
        }

        if (employeeDto.getNote() != null) {
            existingEmployee.setNote(employeeDto.getNote());
        }

        if (employeeDto.getImgUrl() != null) {
            existingEmployee.setImgUrl(employeeDto.getImgUrl());
        }

        return existingEmployee;
    }
}