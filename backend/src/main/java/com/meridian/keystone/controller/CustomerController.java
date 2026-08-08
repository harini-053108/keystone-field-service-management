package com.meridian.keystone.controller;

import com.meridian.keystone.dto.CustomerDto;
import com.meridian.keystone.dto.CustomerRequest;
import com.meridian.keystone.dto.PagedResponse;
import com.meridian.keystone.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer organisation management")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    @Operation(summary = "List customers (searchable, paginated)")
    public PagedResponse<CustomerDto> list(@RequestParam(required = false) String search,
                                           @PageableDefault(size = 20) Pageable pageable) {
        return customerService.list(search, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    @Operation(summary = "Get one customer")
    public CustomerDto get(@PathVariable Long id) {
        return customerService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a customer")
    public CustomerDto create(@Valid @RequestBody CustomerRequest request) {
        return customerService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    @Operation(summary = "Update a customer")
    public CustomerDto update(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return customerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a customer (manager only)")
    public void delete(@PathVariable Long id) {
        customerService.delete(id);
    }
}