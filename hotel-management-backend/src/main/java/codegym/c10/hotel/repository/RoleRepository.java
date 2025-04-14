package codegym.c10.hotel.repository;

import codegym.c10.hotel.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Role findByUsername(String username);
}
