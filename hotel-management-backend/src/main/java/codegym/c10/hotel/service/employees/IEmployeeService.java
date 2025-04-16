package codegym.c10.hotel.service.employees;

import codegym.c10.hotel.entity.Employee;
import codegym.c10.hotel.service.IGenerateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IEmployeeService extends IGenerateService<Employee> {
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    Optional<Employee> findByUserId(Long userId);
    Employee update(Employee employee);
    boolean existsByPhone(String phone);
    boolean existsByIdCard(String idCard);
    boolean existsByPhoneAndIdNot(String phone, Long id);
    boolean existsByIdCardAndIdNot(String idCard, Long id);
}