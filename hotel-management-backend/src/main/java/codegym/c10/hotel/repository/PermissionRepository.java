// hotel/repository/PermissionRepository.java
package codegym.c10.hotel.repository;

import codegym.c10.hotel.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Nên dùng Optional cho các phương thức find đơn lẻ

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    // Tìm permission theo tên (hữu ích để tránh tạo trùng)
    Optional<Permission> findByName(String name);

    // Các phương thức JpaRepository cơ bản (findAll, findById, save, deleteById...) đã có sẵn.
}