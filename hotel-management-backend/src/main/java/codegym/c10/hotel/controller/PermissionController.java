package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.PermissionDto;
import codegym.c10.hotel.service.permission.IPermissionService;
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
@RequestMapping("/api/permissions")
public class PermissionController {
    @Autowired
    private IPermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasPermission('CREATE_PERMISSION')")
    public ResponseEntity<PermissionDto> createPermission(@Valid @RequestBody PermissionDto permissionDto) {
        PermissionDto createdPermission = permissionService.createPermission(permissionDto);
        return new ResponseEntity<>(createdPermission, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasPermission('VIEW_PERMISSION')")
    public ResponseEntity<Page<PermissionDto>> getAllPermissions(Pageable pageable) {
        Page<PermissionDto> permissions = permissionService.findAll(pageable);
        return new ResponseEntity<>(permissions, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('VIEW_PERMISSION')")
    public ResponseEntity<PermissionDto> getPermissionById(@PathVariable Long id) {
        Optional<PermissionDto> permission = permissionService.findById(id);
        return permission.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('UPDATE_PERMISSION')")
    public ResponseEntity<PermissionDto> updatePermission(@PathVariable Long id, @Valid @RequestBody PermissionDto permissionDto) {
        Optional<PermissionDto> updatedPermission = permissionService.updatePermission(id, permissionDto);
        return updatedPermission.map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('DELETE_PERMISSION')")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        try {
            boolean deleted = permissionService.deletePermission(id);
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
}

