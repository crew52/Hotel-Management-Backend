package codegym.c10.hotel.service.employees;

import codegym.c10.hotel.dto.EmployeeDto;
import codegym.c10.hotel.entity.Employee;
import codegym.c10.hotel.service.IGenerateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IEmployeeService extends IGenerateService<Employee> {
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    // Thêm các phương thức mới
    Page<Employee> findAllByDepartmentAndPositionAndDeletedFalse(
            String department, String position, Pageable pageable);

    Page<Employee> findAllByDepartmentAndDeletedFalse(
            String department, Pageable pageable);

    Page<Employee> findAllByPositionAndDeletedFalse(
            String position, Pageable pageable);

    Optional<Employee> findByUserId(Long userId);
    Employee update(Employee employee);
    boolean existsByPhone(String phone);
    boolean existsByIdCard(String idCard);
    boolean existsByPhoneAndIdNot(String phone, Long id);
    boolean existsByIdCardAndIdNot(String idCard, Long id);

    // Các phương thức mới cho mapper service
    Page<EmployeeDto> findEmployeesByFilters(String department, String position, Pageable pageable);
    EmployeeDto findEmployeeDtoById(Long id);
    EmployeeDto createEmployee(EmployeeDto employeeDto);
    EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto);
}