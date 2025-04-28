package codegym.c10.hotel.repository;

import codegym.c10.hotel.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ICustomerRepository extends JpaRepository<Customer, Long> {
    Page<Customer> findAllByDeletedFalse(Pageable pageable);
    Optional<Customer> findByIdAndDeletedFalse(Long id);
}
