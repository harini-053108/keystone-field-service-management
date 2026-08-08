package com.meridian.keystone.service;

import com.meridian.keystone.domain.Customer;
import com.meridian.keystone.dto.CustomerDto;
import com.meridian.keystone.dto.CustomerRequest;
import com.meridian.keystone.dto.PagedResponse;
import com.meridian.keystone.exception.ApiException;
import com.meridian.keystone.repository.CustomerRepository;
import com.meridian.keystone.repository.SiteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final SiteRepository siteRepository;

    public CustomerService(CustomerRepository customerRepository, SiteRepository siteRepository) {
        this.customerRepository = customerRepository;
        this.siteRepository = siteRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<CustomerDto> list(String search, Pageable pageable) {
        Page<Customer> page = (search == null || search.isBlank())
                ? customerRepository.findAll(pageable)
                : customerRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        search.trim(), search.trim(), pageable);
        return PagedResponse.from(page, c -> CustomerDto.from(c, siteRepository.findByCustomerId(c.getId()).size()));
    }

    @Transactional(readOnly = true)
    public CustomerDto get(Long id) {
        return CustomerDto.from(getEntity(id), siteRepository.findByCustomerId(id).size());
    }

    @Transactional
    public CustomerDto create(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.name().trim());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setAddress(request.address());
        return CustomerDto.from(customerRepository.save(customer));
    }

    @Transactional
    public CustomerDto update(Long id, CustomerRequest request) {
        Customer customer = getEntity(id);
        customer.setName(request.name().trim());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
        customer.setAddress(request.address());
        return CustomerDto.from(customerRepository.save(customer), siteRepository.findByCustomerId(id).size());
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = getEntity(id);
        if (!siteRepository.findByCustomerId(id).isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot delete a customer that has sites");
        }
        customerRepository.delete(customer);
    }

    public Customer getEntity(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found"));
    }
}