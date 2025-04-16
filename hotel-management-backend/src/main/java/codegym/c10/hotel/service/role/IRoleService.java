// hotel/service/role/IRoleService.java
package codegym.c10.hotel.service.role;

import codegym.c10.hotel.dto.RoleDto;
import codegym.c10.hotel.entity.Role; // Import Role entity nếu cần trả về entity
import org.springframework.data.domain.Page; // Ví dụ phân trang
import org.springframework.data.domain.Pageable; // Ví dụ phân trang

import java.util.List;
import java.util.Optional;

public interface IRoleService {

    /**
     * Tạo một Role mới.
     * @param roleDto DTO chứa thông tin Role cần tạo (tên, danh sách ID permission).
     * @return RoleDto của Role vừa tạo.
     * @throws RuntimeException Nếu tên Role đã tồn tại hoặc Permission ID không hợp lệ.
     */
    RoleDto createRole(RoleDto roleDto);

    /**
     * Cập nhật thông tin Role.
     * @param id ID của Role cần cập nhật.
     * @param roleDto DTO chứa thông tin mới (tên, danh sách ID permission).
     * @return Optional<RoleDto> chứa Role đã cập nhật, hoặc Optional.empty() nếu không tìm thấy Role.
     * @throws RuntimeException Nếu Permission ID không hợp lệ.
     */
    Optional<RoleDto> updateRole(Long id, RoleDto roleDto);

    /**
     * Xóa một Role.
     * @param id ID của Role cần xóa.
     * @return true nếu xóa thành công, false nếu không tìm thấy Role.
     * @throws RuntimeException Nếu Role đang được sử dụng bởi User (cần xem xét logic).
     */
    boolean deleteRole(Long id);

    /**
     * Tìm Role theo ID.
     * @param id ID của Role.
     * @return Optional<RoleDto> chứa thông tin Role (bao gồm cả Permissions chi tiết).
     */
    Optional<RoleDto> findById(Long id);

     /**
     * Tìm Role theo tên.
     * @param name Tên của Role.
     * @return Optional<RoleDto> chứa thông tin Role.
     */
    Optional<RoleDto> findByName(String name);


    /**
     * Lấy danh sách tất cả Roles (có thể có phân trang).
     * @param pageable Đối tượng chứa thông tin phân trang và sắp xếp.
     * @return Page<RoleDto> chứa danh sách Role.
     */
    Page<RoleDto> findAll(Pageable pageable); // Hoặc List<RoleDto> findAll(); nếu không cần phân trang

    /**
     * Gán một Permission cho một Role.
     * @param roleId ID của Role.
     * @param permissionId ID của Permission.
     * @return true nếu gán thành công.
     * @throws RuntimeException Nếu Role hoặc Permission không tồn tại, hoặc Role đã có Permission đó.
     */
    boolean addPermissionToRole(Long roleId, Long permissionId);

    /**
     * Gỡ một Permission khỏi một Role.
     * @param roleId ID của Role.
     * @param permissionId ID của Permission.
     * @return true nếu gỡ thành công.
     * @throws RuntimeException Nếu Role hoặc Permission không tồn tại.
     */
    boolean removePermissionFromRole(Long roleId, Long permissionId);

     // --- Có thể thêm các phương thức tiện ích khác ---
     // Ví dụ: Lấy danh sách User thuộc về Role này
     // List<UserSummaryDto> findUsersByRoleId(Long roleId);
}