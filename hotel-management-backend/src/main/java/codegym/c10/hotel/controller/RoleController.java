package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.RoleDto;
import codegym.c10.hotel.service.role.IRoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/roles")
public class RoleController {
    @Autowired
    private IRoleService roleService;

    @PostMapping
    @PreAuthorize("hasPermission('CREATE_ROLE')")
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody RoleDto roleDto) {
        RoleDto createdRole = roleService.createRole(roleDto);
        return new ResponseEntity<>(createdRole, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasPermission('VIEW_ROLE')")
    public ResponseEntity<Page<RoleDto>> getAllRoles(Pageable pageable) {
        Page<RoleDto> roles = roleService.findAll(pageable);
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('VIEW_ROLE')")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable Long id) {
        Optional<RoleDto> role = roleService.findById(id);
        return role.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('UPDATE_ROLE')")
    public ResponseEntity<RoleDto> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDto roleDto) {
        Optional<RoleDto> updatedRole = roleService.updateRole(id, roleDto);
        return updatedRole.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('DELETE_ROLE')")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        try {
            boolean deleted = roleService.deleteRole(id);
            if (deleted) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Đã sửa
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Hoặc một status code phù hợp hơn
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Đã sửa
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // Đã sửa
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Đã sửa
        }
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasPermission('UPDATE_ROLE')")
    public ResponseEntity<Void> addPermissionToRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        boolean added = roleService.addPermissionToRole(roleId, permissionId);
        if (added) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Hoặc một status code phù hợp hơn, ví dụ: ALREADY_EXISTS
        }
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasPermission('UPDATE_ROLE')")
    public ResponseEntity<Void> removePermissionFromRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        boolean removed = roleService.removePermissionFromRole(roleId, permissionId);
        if (removed) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Hoặc BAD_REQUEST nếu permission không thuộc role
        }
    }
}
