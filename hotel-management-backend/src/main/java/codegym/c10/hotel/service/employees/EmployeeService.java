package codegym.c10.hotel.service.employees;

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
}