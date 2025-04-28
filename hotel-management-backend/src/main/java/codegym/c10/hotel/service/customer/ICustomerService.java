package codegym.c10.hotel.service.customer;

import codegym.c10.hotel.entity.Customer;
import codegym.c10.hotel.service.IGenerateService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ICustomerService extends IGenerateService<Customer> {
    Page<Customer> findAllByDeletedFalse(Pageable pageable);
    Optional<Customer> findByIdAndDeletedFalse(Long id);
    Customer updateCustomer(Long id, Customer customer);
}
