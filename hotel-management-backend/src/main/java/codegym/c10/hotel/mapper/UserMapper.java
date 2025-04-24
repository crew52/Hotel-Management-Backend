package codegym.c10.hotel.mapper;

import codegym.c10.hotel.dto.UserDto;
import codegym.c10.hotel.entity.Role;
import codegym.c10.hotel.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setLocked(Boolean.parseBoolean(String.valueOf(user.getIsLocked())));
        // Thêm các trường cần thiết khác

        // Map roles nếu cần
        if (user.getRoles() != null) {
            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
            dto.setRoleNames(roleNames);
        }

        return dto;
    }
}
