package codegym.c10.hotel.service.customer;

import codegym.c10.hotel.entity.Customer;
import codegym.c10.hotel.repository.ICustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * CustomerServiceImpl implements the service layer for customer-related operations,
 * including finding, creating, updating, removing customers, and ensuring the uniqueness
 * of fields such as email, phone, and ID card numbers.
 */
@Service
public class CustomerServiceImpl implements ICustomerService {

    @Autowired
    private ICustomerRepository customerRepository;

    /**
     * Retrieves all customers who have not been marked as deleted, with pagination support.
     *
     * @param pageable an object containing pagination information (page number, page size).
     * @return Page<Customer> a paginated list of customers who are not marked as deleted.
     */
    @Override
    public Page<Customer> findAllByDeletedFalse(Pageable pageable) {
        return customerRepository.findAllByDeletedFalse(pageable);
    }

    /**
     * Retrieves a customer by ID, only if the customer has not been marked as deleted.
     *
     * @param id the ID of the customer to retrieve.
     * @return Optional<Customer> a customer wrapped in an Optional, or Optional.empty() if not found.
     */
    @Override
    public Optional<Customer> findByIdAndDeletedFalse(Long id) {
        return customerRepository.findByIdAndDeletedFalse(id);
    }

    /**
     * Returns all customers in the system. If no customers exist, an empty list will be returned.
     *
     * @return Iterable<Customer> a collection of all customers.
     */
    @Override
    public Iterable<Customer> findAll() {
        return customerRepository.findAll();
    }

    /**
     * Saves a new customer in the system. Before saving, checks for the uniqueness of email, phone, and ID card.
     *
     * @param customer the customer to save.
     * @return Customer the saved customer object.
     * @throws IllegalArgumentException if email, phone, or ID card already exist in the system.
     */
    @Override
    public Customer save(Customer customer) {
        validateCustomerUniqueness(customer, null);
        return customerRepository.save(customer);
    }

    /**
     * Retrieves a customer by ID.
     *
     * @param id the ID of the customer to retrieve.
     * @return Optional<Customer> a customer wrapped in an Optional, or Optional.empty() if not found.
     */
    @Override
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    /**
     * Marks a customer as deleted (sets the deleted flag to true).
     * Throws an EntityNotFoundException if the customer is not found.
     *
     * @param id the ID of the customer to mark as deleted.
     * @throws EntityNotFoundException if no customer is found with the given ID.
     */
    @Override
    public void remove(Long id) {
        Customer customer = customerRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + id));
        customer.setDeleted(true);
        customerRepository.save(customer);
    }

    /**
     * Updates an existing customer based on the given ID.
     * Before updating, checks for the uniqueness of email, phone, and ID card.
     *
     * @param id the ID of the customer to update.
     * @param customer the updated customer object.
     * @return Customer the updated customer object.
     * @throws IllegalArgumentException if email, phone, or ID card already exist in the system.
     */
    @Override
    public Customer updateCustomer(Long id, Customer customer) {
        validateCustomerUniqueness(customer, id);
        customer.setId(id);
        return customerRepository.save(customer);
    }

    /**
     * Validates the uniqueness of a customer's email, phone, and ID card number.
     * If any of these fields already exist in the system, an IllegalArgumentException is thrown.
     *
     * @param customer the customer object to validate.
     * @param idToExclude the ID of the customer to exclude from the uniqueness check (useful for updating).
     * @throws IllegalArgumentException if the email, phone, or ID card is already in use by another customer.
     */
    private void validateCustomerUniqueness(Customer customer, Long idToExclude) {
        if (customerRepository.existsByEmailAndIdNot(customer.getEmail(), idToExclude)) {
            throw new IllegalArgumentException("Email is already in use by another customer.");
        }
        if (customerRepository.existsByPhoneAndIdNot(customer.getPhone(), idToExclude)) {
            throw new IllegalArgumentException("Phone number is already in use by another customer.");
        }
        if (customerRepository.existsByIdCardAndIdNot(customer.getIdCard(), idToExclude)) {
            throw new IllegalArgumentException("ID Card number is already in use by another customer.");
        }
    }
}
