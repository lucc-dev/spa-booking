package com.chi.spa.booking.repository;

import com.chi.spa.booking.SpaBookingApplication;
import com.chi.spa.booking.model.Customer;
import com.chi.spa.booking.model.Gender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SpaBookingApplication.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void saveAndFindById_works() {
        Customer customer = new Customer(null, "Amy", "amy@example.com", "0912345678", Gender.FEMALE);

        Customer saved = customerRepository.save(customer);

        assertThat(saved.getId()).isNotNull();
        assertThat(customerRepository.findById(saved.getId())).isPresent();
        assertThat(customerRepository.findById(saved.getId()).orElseThrow().getEmail()).isEqualTo("amy@example.com");
    }
}
