// hotel/service/permission/IPermissionService.java
package codegym.c10.hotel.service.permission;

import codegym.c10.hotel.dto.PermissionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IPermissionService {

    /**
     * Tạo một Permission mới.
     * @param permissionDto DTO chứa tên Permission.
     * @return PermissionDto của Permission vừa tạo.
     * @throws RuntimeException Nếu tên Permission đã tồn tại.
     */
    PermissionDto createPermission(PermissionDto permissionDto);

    /**
     * Cập nhật tên Permission.
     * @param id ID của Permission cần cập nhật.
     * @param permissionDto DTO chứa tên mới.
     * @return Optional<PermissionDto> chứa Permission đã cập nhật, hoặc Optional.empty() nếu không tìm thấy.
     */
    Optional<PermissionDto> updatePermission(Long id, PermissionDto permissionDto);

     /**
     * Xóa một Permission.
     * @param id ID của Permission cần xóa.
     * @return true nếu xóa thành công, false nếu không tìm thấy.
     * @throws RuntimeException Nếu Permission đang được sử dụng bởi Role (cần xem xét logic).
     */
    boolean deletePermission(Long id);

    /**
     * Tìm Permission theo ID.
     * @param id ID của Permission.
     * @return Optional<PermissionDto>.
     */
    Optional<PermissionDto> findById(Long id);

    /**
     * Tìm Permission theo tên.
     * @param name Tên Permission.
     * @return Optional<PermissionDto>.
     */
    Optional<PermissionDto> findByName(String name);

    /**
     * Lấy danh sách tất cả Permissions (có thể có phân trang).
     * @param pageable Đối tượng phân trang.
     * @return Page<PermissionDto>.
     */
    Page<PermissionDto> findAll(Pageable pageable); // Hoặc List<PermissionDto> findAll();

    // --- Có thể thêm các phương thức tiện ích khác ---
    // Ví dụ: Lấy danh sách Role chứa Permission này
    // List<RoleDto> findRolesByPermissionId(Long permissionId);
}