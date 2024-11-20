package com.vaibhav.yumcraftapp.service;

import com.vaibhav.yumcraftapp.dto.CustomerRequest;
import com.vaibhav.yumcraftapp.dto.CustomerResponse;
import com.vaibhav.yumcraftapp.dto.LoginRequest;
import com.vaibhav.yumcraftapp.entity.Customer;
import com.vaibhav.yumcraftapp.exception.CustomerNotFoundException;
import com.vaibhav.yumcraftapp.helper.EncryptionService;
import com.vaibhav.yumcraftapp.helper.JWTHelper;
import com.vaibhav.yumcraftapp.mapper.CustomerMapper;
import com.vaibhav.yumcraftapp.repo.CustomerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepo customerRepo;
    private final CustomerMapper customerMapper;
    private final EncryptionService encryptionService;
    private final JWTHelper jwtHelper;
    public String createCustomer(CustomerRequest request) {
        Customer customer = customerMapper.toCustomer(request);

        // Save encoded Password


        customer.setPassword(encryptionService.encode(customer.getPassword()));

        customerRepo.save(customer);
        return "Customer Created Successfully";
    }

    public Customer getCustomer(String email) {
        return customerRepo.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException(
                        format("Cannot update Customer:: No customer found with the provided ID:: %s", email)
                ));
    }

    public CustomerResponse retrieveCustomer(String email) {
        Customer customer = getCustomer(email);
        return customerMapper.toCustomerResponse(customer);
    }

    public String login(LoginRequest request) {
        Customer customer = getCustomer(request.email());
        if(!encryptionService.validates(request.password(), customer.getPassword())) {
            return "Wrong Password or Email";
        }

        return jwtHelper.generateToken(request.email());
    }

    public void updateCustomer(String email, CustomerRequest request) {
        Customer customer = customerRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        // Update fields only if they are not null
        if (request.firstName() != null) {
            customer.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            customer.setLastName(request.lastName());
        }
        if (request.address() != null) {
            customer.setAddress(request.address());
        }
        if (request.city() != null) {
            customer.setCity(request.city());
        }
        if (request.pincode() != null) {
            customer.setPincode(request.pincode());
        }
        customerRepo.save(customer);
    }
    public void deleteCustomer(String email) {
        Customer customer = customerRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customerRepo.delete(customer);
    }
    public String validateAndExtractEmail(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid Authorization header");
        }
        String token = authHeader.substring(7);
        if (!jwtHelper.isTokenValid(token)) {
            throw new RuntimeException("Invalid or expired token");
        }
        return jwtHelper.extractEmail(token);
    }
}
