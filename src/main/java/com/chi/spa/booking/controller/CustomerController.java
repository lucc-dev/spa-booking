package com.chi.spa.booking.controller;

import com.chi.spa.booking.model.Booking;
import com.chi.spa.booking.model.Customer;
import com.chi.spa.booking.service.BookingService;
import com.chi.spa.booking.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        boolean isEmailEmpty = (customer.getEmail() == null || customer.getEmail().trim().isEmpty());
        boolean isPhoneEmpty = (customer.getPhoneNumber() == null || customer.getPhoneNumber().trim().isEmpty());

        if (isEmailEmpty && isPhoneEmpty) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Customer created = customerService.createCustomer(customer);
            if (created == null) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers(){
        try{
            List<Customer> customers = customerService.getAllCustomers();
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id){
        try{
            Customer customer = customerService.getCustomerById(id);
            if (customer == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/{id}/bookings")
    public ResponseEntity<List<Booking>> getCustomerBookings(@PathVariable Long id) {
        try {
            Customer customer = customerService.getCustomerById(id);
            if (customer == null) {
                return ResponseEntity.notFound().build();
            }
            List<Booking> bookings = bookingService.getBookingsByCustomerId(id);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        try {
            customer.setId(id);
            Customer updated = customerService.updateCustomer(customer);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomerById(@PathVariable Long id) {
        try {
            boolean deleted = customerService.deleteCustomerById(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
