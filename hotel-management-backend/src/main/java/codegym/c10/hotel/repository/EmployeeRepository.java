package codegym.c10.hotel.repository;

import codegym.c10.hotel.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Page<Employee> findAllByDeletedFalse(Pageable pageable);
    Optional<Employee> findByIdAndDeletedFalse(Long id);
    Optional<Employee> findByUserId(Long userId);
    Optional<Employee> findByPhoneAndDeletedFalse(String phone);
    Optional<Employee> findByIdCardAndDeletedFalse(String idCard);
    boolean existsByPhone(String phone);
    boolean existsByIdCard(String idCard);
    boolean existsByPhoneAndIdNot(String phone, Long id);
    boolean existsByIdCardAndIdNot(String idCard, Long id);
    
    // Các phương thức tìm kiếm không phân biệt deleted
    Page<Employee> findAllByDepartmentAndPosition(String department, String position, Pageable pageable);
    Page<Employee> findAllByDepartment(String department, Pageable pageable);
    Page<Employee> findAllByPosition(String position, Pageable pageable);
    
    // Các phương thức tìm kiếm chỉ với deleted=false
    Page<Employee> findAllByDepartmentAndPositionAndDeletedFalse(
            String department, String position, Pageable pageable);

    Page<Employee> findAllByDepartmentAndDeletedFalse(
            String department, Pageable pageable);

    Page<Employee> findAllByPositionAndDeletedFalse(
            String position, Pageable pageable);
}
