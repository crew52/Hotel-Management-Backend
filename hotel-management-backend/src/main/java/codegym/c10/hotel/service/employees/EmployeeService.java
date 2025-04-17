package codegym.c10.hotel.service.employees;

import codegym.c10.hotel.dto.EmployeeDto;
import codegym.c10.hotel.entity.Employee;
import codegym.c10.hotel.entity.User;
import codegym.c10.hotel.repository.EmployeeRepository;
import codegym.c10.hotel.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService implements IEmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmployeeMapperService employeeMapperService;

    @Override
    public Iterable<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Page<Employee> findAllByDeletedFalse(Pageable pageable) {
        return employeeRepository.findAllByDeletedFalse(pageable);
    }

    @Override
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findByIdAndDeletedFalse(id);
    }

    @Override
    public Optional<Employee> findByUserId(Long userId) {
        return employeeRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Employee save(Employee employee) {
        // Validate if user exists
        Long userId = employee.getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        employee.setUser(user);
        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public Employee update(Employee employee) {
        // Check if employee exists
        employeeRepository.findByIdAndDeletedFalse(employee.getId())
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employee.getId()));

        // Validate if user exists
        Long userId = employee.getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        employee.setUser(user);
        return employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public void remove(Long id) {
        Optional<Employee> optionalEmployee = employeeRepository.findByIdAndDeletedFalse(id);

        if (optionalEmployee.isPresent()) {
            Employee employee = optionalEmployee.get();
            employee.setDeleted(true);
            employeeRepository.save(employee);
        } else {
            throw new EntityNotFoundException("Employee not found with id: " + id);
        }
    }

    @Override
    public boolean existsByPhone(String phone) {
        return employeeRepository.existsByPhone(phone);
    }

    @Override
    public boolean existsByIdCard(String idCard) {
        return employeeRepository.existsByIdCard(idCard);
    }

    @Override
    public boolean existsByPhoneAndIdNot(String phone, Long id) {
        return employeeRepository.existsByPhoneAndIdNot(phone, id);
    }

    @Override
    public boolean existsByIdCardAndIdNot(String idCard, Long id) {
        return employeeRepository.existsByIdCardAndIdNot(idCard, id);
    }

    @Override
    public Page<Employee> findAllByDepartmentAndPositionAndDeletedFalse(
            String department, String position, Pageable pageable) {
        return employeeRepository.findAllByDepartmentAndPositionAndDeletedFalse(
                department, position, pageable);
    }

    @Override
    public Page<Employee> findAllByDepartmentAndDeletedFalse(
            String department, Pageable pageable) {
        return employeeRepository.findAllByDepartmentAndDeletedFalse(department, pageable);
    }

    @Override
    public Page<Employee> findAllByPositionAndDeletedFalse(
            String position, Pageable pageable) {
        return employeeRepository.findAllByPositionAndDeletedFalse(position, pageable);
    }

    @Override
    public Page<EmployeeDto> findEmployeesByFilters(String department, String position, Pageable pageable) {
        Page<Employee> employeesPage;

        if (department != null && position != null) {
            employeesPage = employeeRepository.findAllByDepartmentAndPositionAndDeletedFalse(
                    department, position, pageable);
        } else if (department != null) {
            employeesPage = employeeRepository.findAllByDepartmentAndDeletedFalse(department, pageable);
        } else if (position != null) {
            employeesPage = employeeRepository.findAllByPositionAndDeletedFalse(position, pageable);
        } else {
            employeesPage = employeeRepository.findAllByDeletedFalse(pageable);
        }

        return employeesPage.map(employeeMapperService::convertToDto);
    }

    @Override
    public EmployeeDto findEmployeeDtoById(Long id) {
        Employee employee = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));
        return employeeMapperService.convertToDto(employee);
    }

    @Override
    public EmployeeDto createEmployee(EmployeeDto employeeDto) {
        // Validate
        if (employeeDto.getPhone() != null && existsByPhone(employeeDto.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        if (employeeDto.getIdCard() != null && existsByIdCard(employeeDto.getIdCard())) {
            throw new IllegalArgumentException("ID card already exists");
        }

        // Convert to entity
        Employee employee = employeeMapperService.convertToEntity(employeeDto);

        // Save entity
        Employee savedEmployee = save(employee);

        // Convert back to DTO
        return employeeMapperService.convertToDto(savedEmployee);
    }

    @Override
    public EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto) {
        // Validate exists
        Employee existingEmployee = findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));

        // Validate unique constraints
        if (employeeDto.getPhone() != null &&
                existsByPhoneAndIdNot(employeeDto.getPhone(), id)) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        if (employeeDto.getIdCard() != null &&
                existsByIdCardAndIdNot(employeeDto.getIdCard(), id)) {
            throw new IllegalArgumentException("ID card already exists");
        }

        // Set ID
        employeeDto.setId(id);

        // Convert to entity
        Employee employee = employeeMapperService.convertToEntity(employeeDto);

        // Save
        Employee updatedEmployee = update(employee);

        // Convert back to DTO
        return employeeMapperService.convertToDto(updatedEmployee);
    }
}