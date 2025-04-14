// hotel/service/role/RoleService.java (Ví dụ)
package codegym.c10.hotel.service.role;


import codegym.c10.hotel.dto.PermissionDto;
import codegym.c10.hotel.dto.RoleDto;
import codegym.c10.hotel.entity.Permission;
import codegym.c10.hotel.entity.Role;
import codegym.c10.hotel.repository.PermissionRepository;
import codegym.c10.hotel.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException; // Exception chuẩn
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Quan trọng cho các thao tác sửa đổi

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService implements IRoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    // --- Helper Method to convert Entity to DTO ---
    private RoleDto mapToRoleDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        // Thống nhất trả về tên Role không có prefix "ROLE_" trong DTO
        dto.setName(role.getName().startsWith("ROLE_") ? role.getName().substring(5) : role.getName());

        if (role.getPermissions() != null) {
            dto.setPermissions(role.getPermissions().stream()
                    .map(this::mapToPermissionDto)
                    .collect(Collectors.toSet()));
            // Có thể thêm permissionIds nếu cần
             dto.setPermissionIds(role.getPermissions().stream()
                    .map(Permission::getId)
                    .collect(Collectors.toSet()));
        } else {
            dto.setPermissions(new HashSet<>());
            dto.setPermissionIds(new HashSet<>());
        }
        return dto;
    }
     private PermissionDto mapToPermissionDto(Permission permission) {
        return new PermissionDto(permission.getId(), permission.getName());
    }

    // --- Implementations ---

    @Override
    @Transactional // Đảm bảo tính nhất quán
    public RoleDto createRole(RoleDto roleDto) {
        // Thống nhất lưu tên Role trong DB với prefix "ROLE_"
        String roleNameInDb = roleDto.getName().startsWith("ROLE_") ? roleDto.getName() : "ROLE_" + roleDto.getName().toUpperCase();

        if (roleRepository.findByName(roleNameInDb) != null) {
            throw new IllegalArgumentException("Role name '" + roleDto.getName() + "' already exists.");
        }

        Role newRole = new Role();
        newRole.setName(roleNameInDb);

        Set<Permission> permissions = findAndValidatePermissions(roleDto.getPermissionIds());
        newRole.setPermissions(permissions);

        Role savedRole = roleRepository.save(newRole);
        return mapToRoleDto(savedRole);
    }


    @Override
    @Transactional
    public Optional<RoleDto> updateRole(Long id, RoleDto roleDto) {
         Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));

        // Kiểm tra và chuẩn hóa tên Role mới (nếu có thay đổi)
        if (roleDto.getName() != null && !roleDto.getName().isBlank()) {
             String newRoleNameInDb = roleDto.getName().startsWith("ROLE_") ? roleDto.getName() : "ROLE_" + roleDto.getName().toUpperCase();
             // Kiểm tra nếu tên mới khác tên cũ và đã tồn tại ở role khác
             if (!existingRole.getName().equalsIgnoreCase(newRoleNameInDb) && roleRepository.findByName(newRoleNameInDb) != null) {
                 throw new IllegalArgumentException("Role name '" + roleDto.getName() + "' already exists.");
             }
             existingRole.setName(newRoleNameInDb);
        }

        // Cập nhật permissions nếu có trong DTO
        if (roleDto.getPermissionIds() != null) {
             Set<Permission> permissions = findAndValidatePermissions(roleDto.getPermissionIds());
             existingRole.setPermissions(permissions);
        }


        Role updatedRole = roleRepository.save(existingRole);
        return Optional.of(mapToRoleDto(updatedRole));
    }


    @Override
    @Transactional
    public boolean deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + id));

        // **QUAN TRỌNG**: Kiểm tra xem Role có đang được User nào sử dụng không.
        // Nếu có, bạn có thể không cho xóa hoặc phải xử lý gỡ Role khỏi User trước.
        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
             throw new IllegalStateException("Cannot delete role with ID " + id + " because it is assigned to users.");
             // Hoặc: Gỡ role khỏi users trước khi xóa (cần UserService)
             // userService.removeRoleFromAllUsers(id);
        }

        // Xóa các liên kết trong bảng role_permissions trước (JPA thường tự xử lý nếu cascade đúng)
        // Hoặc xóa thủ công nếu cần: role.getPermissions().clear(); roleRepository.save(role);

        roleRepository.delete(role);
        return true; // Hoặc kiểm tra lại xem còn tồn tại không roleRepository.existsById(id);
    }


    @Override
    @Transactional(readOnly = true) // Chỉ đọc
    public Optional<RoleDto> findById(Long id) {
        return roleRepository.findById(id).map(this::mapToRoleDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoleDto> findByName(String name) {
        // Nhớ chuẩn hóa tên trước khi tìm
         String roleNameInDb = name.startsWith("ROLE_") ? name : "ROLE_" + name.toUpperCase();
        Role role = roleRepository.findByName(roleNameInDb);
        return Optional.ofNullable(role).map(this::mapToRoleDto);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<RoleDto> findAll(Pageable pageable) {
        return roleRepository.findAll(pageable).map(this::mapToRoleDto);
    }


    @Override
    @Transactional
    public boolean addPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));

        if (role.getPermissions().contains(permission)) {
             // Hoặc trả về false, hoặc không làm gì cả
             // throw new IllegalArgumentException("Role already has this permission.");
             return false; // Đã có quyền này rồi
        }

        role.getPermissions().add(permission);
        roleRepository.save(role);
        return true;
    }


    @Override
    @Transactional
    public boolean removePermissionFromRole(Long roleId, Long permissionId) {
         Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + permissionId));

        boolean removed = role.getPermissions().remove(permission);
        if (removed) {
            roleRepository.save(role);
        }
        return removed;
    }

     // --- Helper method to validate and fetch permissions ---
    private Set<Permission> findAndValidatePermissions(Set<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return new HashSet<>(); // Trả về set rỗng nếu không có ID nào được cung cấp
        }
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
        // Kiểm tra xem tất cả các ID yêu cầu có được tìm thấy không
        if (permissions.size() != permissionIds.size()) {
            Set<Long> foundIds = permissions.stream().map(Permission::getId).collect(Collectors.toSet());
            permissionIds.removeAll(foundIds); // Giữ lại những ID không tìm thấy
            throw new EntityNotFoundException("Could not find permissions with IDs: " + permissionIds);
        }
        return permissions;
    }
}