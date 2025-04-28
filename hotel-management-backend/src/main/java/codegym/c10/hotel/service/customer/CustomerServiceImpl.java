package codegym.c10.hotel.service.customer;

import codegym.c10.hotel.eNum.RoomStatus;
import codegym.c10.hotel.entity.Customer;
import codegym.c10.hotel.entity.Room;
import codegym.c10.hotel.repository.ICustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements ICustomerService {

    @Autowired
    private ICustomerRepository customerRepository;

    @Override
    public Page<Customer> findAllByDeletedFalse(Pageable pageable) {
        return customerRepository.findAllByDeletedFalse(pageable);
    }

    @Override
    public Optional<Customer> findByIdAndDeletedFalse(Long id) {
        return customerRepository.findByIdAndDeletedFalse(id);
    }

    @Override
    public Iterable<Customer> findAll() {
        return null;
    }

    @Override
    public Customer save(Customer T) {
        return null;
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public void remove(Long id) {
        Customer customer = customerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));
        customer.setDeleted(true);
        customerRepository.save(customer);
    }
}
