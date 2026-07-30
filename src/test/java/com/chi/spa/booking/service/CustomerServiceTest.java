package com.chi.spa.booking.service;

import com.chi.spa.booking.SpaBookingApplication;
import com.chi.spa.booking.model.Customer;
import com.chi.spa.booking.model.Gender;
import com.chi.spa.booking.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SpaBookingApplication.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class CustomerServiceTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void clearData() {
        customerRepository.deleteAll();
    }

    @Test
    void createCustomer_savesCustomer() {
        Customer customer = new Customer(null, "Amy", "amy@example.com", "0912345678", Gender.FEMALE);

        Customer result = customerService.createCustomer(customer);

        assertThat(result.getId()).isNotNull();
        assertThat(customerRepository.findById(result.getId())).isPresent();
    }

    @Test
    void getAllCustomers_returnsAllCustomers() {
        customerRepository.save(new Customer(null, "Amy", "amy@example.com", "0912345678", Gender.FEMALE));

        List<Customer> result = customerService.getAllCustomers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Amy");
    }

    @Test
    void getCustomerById_returnsCustomerWhenFound() {
        Customer saved = customerRepository.save(new Customer(null, "Amy", "amy@example.com", "0912345678", Gender.FEMALE));

        Customer result = customerService.getCustomerById(saved.getId());

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("amy@example.com");
    }

    @Test
    void getCustomerById_returnsNullWhenMissing() {
        assertThat(customerService.getCustomerById(999L)).isNull();
    }

    @Test
    void updateCustomer_savesCustomer() {
        Customer saved = customerRepository.save(new Customer(null, "Amy", "amy@example.com", "0912345678", Gender.FEMALE));
        saved.setName("Amy Updated");

        Customer result = customerService.updateCustomer(saved);

        assertThat(result.getName()).isEqualTo("Amy Updated");
        assertThat(customerRepository.findById(saved.getId()).orElseThrow().getName()).isEqualTo("Amy Updated");
    }

    @Test
    void deleteCustomerById_deletesWhenExists() {
        Customer saved = customerRepository.save(new Customer(null, "Amy", "amy@example.com", "0912345678", Gender.FEMALE));

        boolean result = customerService.deleteCustomerById(saved.getId());

        assertThat(result).isTrue();
        assertThat(customerRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void deleteCustomerById_returnsFalseWhenMissing() {
        assertThat(customerService.deleteCustomerById(999L)).isFalse();
    }
}
