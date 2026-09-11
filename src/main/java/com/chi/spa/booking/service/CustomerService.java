package com.chi.spa.booking.service;

import com.chi.spa.booking.model.Customer;
import com.chi.spa.booking.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer createCustomer(Customer customer) {
        // TODO: 這裡可以加入「檢查手機或信箱是否已被其他人註冊」的重複校驗邏輯
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        return customer.orElse(null);
    }

    public Customer updateCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public boolean deleteCustomerById(Long id) {
        if (customerRepository.existsById(id)){
            customerRepository.deleteById(id);
            return true;
        }
        return false;
    }
    /**
     * 依電話或 Email 尋找既有顧客，找不到就自動建立一筆新的。
     * 用於預約流程中自動關聯顧客身分，不用讓顧客額外先手動註冊。
     */
    public Customer findOrCreateCustomer(String name, String phoneNumber, String email) {
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            Optional<Customer> byPhone = customerRepository.findByPhoneNumber(phoneNumber);
            if (byPhone.isPresent()) {
                return byPhone.get();
            }
        }

        if (email != null && !email.isBlank()) {
            Optional<Customer> byEmail = customerRepository.findByEmail(email);
            if (byEmail.isPresent()) {
                return byEmail.get();
            }
        }

        Customer newCustomer = new Customer();
        newCustomer.setName(name);
        newCustomer.setPhoneNumber((phoneNumber != null && !phoneNumber.isBlank()) ? phoneNumber : null);
        newCustomer.setEmail((email != null && !email.isBlank()) ? email : null);
        return customerRepository.save(newCustomer);
    }
}