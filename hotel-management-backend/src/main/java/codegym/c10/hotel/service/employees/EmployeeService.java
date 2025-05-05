package codegym.c10.hotel.service.employees;

import codegym.c10.hotel.annotation.LogActivity;
import codegym.c10.hotel.dto.EmployeeDto;
import codegym.c10.hotel.entity.Employee;
import codegym.c10.hotel.entity.User;
import codegym.c10.hotel.repository.EmployeeRepository;
import codegym.c10.hotel.repository.UserRepository;
import codegym.c10.hotel.service.uploadFile.StorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeService implements IEmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final EmployeeMapperService employeeMapperService;
    @Autowired
    private StorageService storageService;

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
    @LogActivity(action = "EMPLOYEE_SAVE", description = "Thêm nhân viên mới vào hệ thống")
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
    @LogActivity(action = "EMPLOYEE_UPDATE", description = "Cập nhật thông tin nhân viên")
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
        // Validate số điện thoại và CMND độc nhất
        if (employeeDto.getPhone() != null && existsByPhone(employeeDto.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists");
        }

        if (employeeDto.getIdCard() != null && existsByIdCard(employeeDto.getIdCard())) {
            throw new IllegalArgumentException("ID card already exists");
        }

        // Validate user_id
        validateUserIdForCreate(employeeDto.getUserId());

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

        // Validate user_id (nếu đang thay đổi user_id)
        if (employeeDto.getUserId() != null && 
                !employeeDto.getUserId().equals(existingEmployee.getUser().getId())) {
            validateUserIdForUpdate(employeeDto.getUserId(), id);
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
    
    /**
     * Kiểm tra user_id khi tạo mới nhân viên
     */
    private void validateUserIdForCreate(Long userId) {
        // Kiểm tra user có tồn tại không
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        
        // Kiểm tra user đã được liên kết với nhân viên nào khác chưa
        Optional<Employee> existingEmployee = employeeRepository.findByUserId(userId);
        if (existingEmployee.isPresent()) {
            throw new IllegalArgumentException("User is already linked to another employee");
        }
    }
    
    /**
     * Kiểm tra user_id khi cập nhật nhân viên
     */
    private void validateUserIdForUpdate(Long userId, Long employeeId) {
        // Kiểm tra user có tồn tại không
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        
        // Kiểm tra user đã được liên kết với nhân viên nào khác chưa
        Optional<Employee> existingEmployee = employeeRepository.findByUserId(userId);
        if (existingEmployee.isPresent() && !existingEmployee.get().getId().equals(employeeId)) {
            throw new IllegalArgumentException("User is already linked to another employee");
        }
    }

    @Override
    public EmployeeDto createEmployeeWithImage(EmployeeDto dto, MultipartFile imageFile) throws IOException {
        Employee employee = employeeMapperService.convertToEntity(dto);

        if (imageFile != null && !imageFile.isEmpty()) {
            // Lưu file ảnh vào hệ thống/tệp/cơ sở dữ liệu (tuỳ mục đích)
            String imagePath = storageService.storeWithUUID(imageFile, "employees"); // ví dụ lưu file ảnh
            employee.setImgUrl(imagePath); // cập nhật đường dẫn trong entity
        }

        employeeRepository.save(employee);
        return employeeMapperService.convertToDto(employee);
    }

    @Override
    public EmployeeDto updateEmployeeWithImage(Long id, EmployeeDto employeeDto, MultipartFile imageFile) {
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

        // Validate user_id (nếu đang thay đổi user_id)
        if (employeeDto.getUserId() != null &&
                !employeeDto.getUserId().equals(existingEmployee.getUser().getId())) {
            validateUserIdForUpdate(employeeDto.getUserId(), id);
        }

        // Set ID để đảm bảo convert chính xác
        employeeDto.setId(id);

        // Convert DTO to entity nhưng giữ lại trường ảnh cũ nếu chưa có ảnh mới
        Employee employeeToUpdate = employeeMapperService.convertToEntity(employeeDto);
        employeeToUpdate.setImgUrl(existingEmployee.getImgUrl()); // giữ lại ảnh cũ nếu không cập nhật ảnh mới

        if (imageFile != null && !imageFile.isEmpty()) {
            // ✅ Xóa ảnh cũ trước khi cập nhật ảnh mới
            String oldImageUrl = existingEmployee.getImgUrl();
            if (oldImageUrl != null && storageService.exists(oldImageUrl)) {
                storageService.deleteFile(oldImageUrl);
            }

            String imageUrl = storageService.storeWithUUID(imageFile, "employees");
            employeeToUpdate.setImgUrl(imageUrl);
        }

        // Save
        Employee updatedEmployee = update(employeeToUpdate);

        // Convert back to DTO
        return employeeMapperService.convertToDto(updatedEmployee);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return employeeRepository.existsByUser_Id(userId);
    }

    @Override
    public boolean existsByUserIdAndIdNot(Long userId, Long id) {
        return employeeRepository.existsByUser_IdAndIdNot(userId, id);
    }
}