package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.ApiResponse;
import codegym.c10.hotel.dto.RoleDto;
import codegym.c10.hotel.dto.UserDto;
import codegym.c10.hotel.dto.UserStatusDto;
import codegym.c10.hotel.entity.Room;
import codegym.c10.hotel.entity.User;
import codegym.c10.hotel.mapper.UserMapper;
import codegym.c10.hotel.service.user.IUserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @Autowired
    private final UserMapper userMapper;

    @GetMapping()
    @PreAuthorize("@securityService.hasPermission('VIEW_USER')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        // Lấy danh sách User
        Iterable<User> userIterable = userService.findAll();
        
        // Chuyển đổi từ User sang UserDto để chỉ trả về các thông tin cần thiết
        List<UserDto> userDtos = StreamSupport.stream(userIterable.spliterator(), false)
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(userDtos);
    }
    
    /**
     * Chuyển đổi User entity thành UserDto (chỉ chứa thông tin cần thiết)
     */
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        
        // Lấy tên các vai trò
        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());
        dto.setRoleNames(roleNames);
        
        return dto;
    }

    @PutMapping("/{id}/lock-account")
    @PreAuthorize("@securityService.hasPermission('UPDATE_USER')")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UserStatusDto statusDto) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
            
            // Cập nhật trạng thái khoá
            user.setIsLocked(statusDto.isLocked());
            userService.update(user);
            
            String message = statusDto.isLocked() ? 
                    "User account has been locked successfully" : 
                    "User account has been unlocked successfully";
            
            return ResponseEntity.ok(new ApiResponse(true, message));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error updating user status: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.hasPermission('VIEW_USER')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> new ResponseEntity<>(userMapper.toDto(user), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
} 