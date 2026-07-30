package com.chi.spa.booking.controller;

import com.chi.spa.booking.SpaBookingApplication;
import com.chi.spa.booking.model.Customer;
import com.chi.spa.booking.model.Gender;
import com.chi.spa.booking.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest(classes = SpaBookingApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@Transactional
class CustomerControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CustomerRepository customerRepository;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context).build();
        customerRepository.deleteAll();
    }

    @Test
    void createCustomer_returnsBadRequestWhenContactMissing() throws Exception {
        Customer customer = new Customer(null, "Amy", "", "", Gender.FEMALE);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCustomer_returnsOkWhenContactPresent() throws Exception {
        Customer customer = new Customer(null, "Amy", "amy@example.com", "", Gender.FEMALE);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Amy"))
                .andExpect(jsonPath("$.email").value("amy@example.com"));
    }

    @Test
    void getAllCustomers_returnsOk() throws Exception {
        customerRepository.save(new Customer(null, "Amy", "amy@example.com", "0912345678", Gender.FEMALE));

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Amy"));
    }

    @Test
    void getCustomerById_returnsNotFoundWhenMissing() throws Exception {
        mockMvc.perform(get("/customers/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCustomer_returnsOkAndUsesPathId() throws Exception {
        Customer saved = customerRepository.save(new Customer(null, "Amy", "amy@example.com", "0912345678", Gender.FEMALE));
        Customer customer = new Customer(null, "Amy Updated", "amy@example.com", "0912345678", Gender.FEMALE);

        mockMvc.perform(put("/customers/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId().intValue()))
                .andExpect(jsonPath("$.name").value("Amy Updated"));
    }

    @Test
    void deleteCustomerById_returnsNoContent() throws Exception {
        Customer saved = customerRepository.save(new Customer(null, "Amy", "amy@example.com", "0912345678", Gender.FEMALE));

        mockMvc.perform(delete("/customers/{id}", saved.getId()))
                .andExpect(status().isNoContent());
    }
}
