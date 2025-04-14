// hotel/service/permission/PermissionService.java
package codegym.c10.hotel.service.permission;

import codegym.c10.hotel.dto.PermissionDto;
import codegym.c10.hotel.entity.Permission;
import codegym.c10.hotel.repository.PermissionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PermissionService implements IPermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    // --- Helper Method to convert Entity to DTO ---
    private PermissionDto mapToPermissionDto(Permission permission) {
        if (permission == null) {
            return null;
        }
        return new PermissionDto(permission.getId(), permission.getName());
    }

    @Override
    @Transactional // Ghi chú: Thao tác tạo mới nên có Transactional
    public PermissionDto createPermission(PermissionDto permissionDto) {
        // 1. Kiểm tra xem tên quyền đã tồn tại chưa
        if (permissionRepository.findByName(permissionDto.getName()).isPresent()) {
            throw new IllegalArgumentException("Permission name '" + permissionDto.getName() + "' already exists.");
        }

        // 2. Tạo đối tượng Permission mới
        Permission newPermission = new Permission();
        newPermission.setName(permissionDto.getName());
        // Các trường khác nếu có (ví dụ: description)

        // 3. Lưu vào database
        Permission savedPermission = permissionRepository.save(newPermission);

        // 4. Chuyển đổi lại thành DTO để trả về
        return mapToPermissionDto(savedPermission);
    }

    @Override
    @Transactional // Ghi chú: Thao tác cập nhật nên có Transactional
    public Optional<PermissionDto> updatePermission(Long id, PermissionDto permissionDto) {
        // 1. Tìm Permission hiện có bằng ID
        Permission existingPermission = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + id));

        // 2. Kiểm tra xem tên mới (nếu có và khác tên cũ) có bị trùng không
        String newName = permissionDto.getName();
        if (newName != null && !newName.isBlank() && !newName.equalsIgnoreCase(existingPermission.getName())) {
            if (permissionRepository.findByName(newName).isPresent()) {
                throw new IllegalArgumentException("Permission name '" + newName + "' already exists.");
            }
            existingPermission.setName(newName); // Cập nhật tên nếu hợp lệ
        }
        // Cập nhật các trường khác nếu cần

        // 3. Lưu thay đổi
        Permission updatedPermission = permissionRepository.save(existingPermission);

        // 4. Trả về DTO đã cập nhật
        return Optional.of(mapToPermissionDto(updatedPermission));
    }

    @Override
    @Transactional // Ghi chú: Thao tác xóa nên có Transactional
    public boolean deletePermission(Long id) {
        // 1. Tìm Permission để đảm bảo nó tồn tại trước khi xóa
        Permission permissionToDelete = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + id));

        // 2. **QUAN TRỌNG**: Kiểm tra xem Permission này có đang được Role nào sử dụng không.
        //    Nếu đang được sử dụng, không nên xóa để tránh lỗi hoặc hành vi không mong muốn.
        if (permissionToDelete.getRoles() != null && !permissionToDelete.getRoles().isEmpty()) {
            throw new IllegalStateException("Cannot delete permission with ID " + id + " because it is currently assigned to one or more roles.");
            // Hoặc bạn có thể chọn cách gỡ bỏ permission này khỏi tất cả các role trước khi xóa,
            // nhưng việc này cần cẩn thận và có thể cần sự can thiệp của RoleService.
        }

        // 3. Nếu không có Role nào sử dụng, tiến hành xóa
        permissionRepository.delete(permissionToDelete);

        // 4. Kiểm tra lại (tùy chọn) xem đã xóa thành công chưa
        return !permissionRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true) // Ghi chú: Thao tác chỉ đọc, readOnly = true để tối ưu
    public Optional<PermissionDto> findById(Long id) {
        return permissionRepository.findById(id).map(this::mapToPermissionDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PermissionDto> findByName(String name) {
        return permissionRepository.findByName(name).map(this::mapToPermissionDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionDto> findAll(Pageable pageable) {
        // Page<Entity> có phương thức map để chuyển đổi thành Page<DTO>
        return permissionRepository.findAll(pageable).map(this::mapToPermissionDto);
    }
}