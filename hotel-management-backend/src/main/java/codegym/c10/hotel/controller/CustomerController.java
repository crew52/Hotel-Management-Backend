package codegym.c10.hotel.controller;
import codegym.c10.hotel.entity.Customer;
import codegym.c10.hotel.exception.ErrorResponse;
import codegym.c10.hotel.service.customer.ICustomerService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * CustomerController is a REST controller that manages customer-related operations.
 * It handles HTTP requests for creating, updating, deleting, and retrieving customer data.
 * The controller provides endpoints to interact with customer resources.
 */
@RestController
@RequestMapping("/api/customers")
@CrossOrigin("*")
public class CustomerController {

    @Autowired
    private ICustomerService customerService;

    /**
     * Retrieves a paginated list of all customers who are not marked as deleted.
     *
     * @param page the page number to retrieve (default is 0).
     * @param size the number of customers per page (default is 10).
     * @return ResponseEntity<Page<Customer>> a paginated list of customers.
     */
    @GetMapping
    public ResponseEntity<Page<Customer>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Customer> customers = customerService.findAllByDeletedFalse(pageable);
        return ResponseEntity.ok(customers);
    }

    /**
     * Retrieves a customer by its ID, only if the customer is not marked as deleted.
     *
     * @param id the ID of the customer to retrieve.
     * @return ResponseEntity<Customer> the customer object, or a NOT_FOUND status if not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomersById(@PathVariable Long id) {
        return customerService.findByIdAndDeletedFalse(id)
                .map(customer -> new ResponseEntity<>(customer, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Deletes a customer by its ID and marks the customer as deleted.
     *
     * @param id the ID of the customer to delete.
     * @return ResponseEntity<?> a NO_CONTENT status if successful, or an error message if customer not found.
     * @throws EntityNotFoundException if the customer with the given ID does not exist.
     * @throws IllegalStateException if there is any issue with the deletion process.
     */
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> removeCustomer(@PathVariable Long id) {
        try {
            customerService.remove(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message","Customer not found with id: " + id));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Creates a new customer.
     *
     * @param customer the customer data to create.
     * @return ResponseEntity<?> the newly created customer, or a BAD_REQUEST error message if validation fails.
     * @throws IllegalArgumentException if the customer data is invalid (e.g., email, phone, or ID card already exists).
     */
    @PostMapping
    public ResponseEntity<?> createCustomer(@Valid @RequestBody Customer customer) {
        try {
            Customer savedCustomer = customerService.save(customer);
            return new ResponseEntity<>(savedCustomer, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Updates an existing customer by its ID.
     *
     * @param id the ID of the customer to update.
     * @param customer the updated customer data.
     * @return ResponseEntity<?> the updated customer, or an error message if validation or customer not found fails.
     * @throws IllegalArgumentException if the customer data is invalid (e.g., email, phone, or ID card already exists).
     * @throws EntityNotFoundException if the customer with the given ID is not found.
     */
    @PutMapping("/{id}/edit")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, @Valid @RequestBody Customer customer) {
        try {
            Customer updatedCustomer = customerService.updateCustomer(id, customer);
            return new ResponseEntity<>(updatedCustomer, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Customer not found with id: " + id));
        }
    }
}
