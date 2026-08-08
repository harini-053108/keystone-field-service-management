package com.meridian.keystone.dto;

import com.meridian.keystone.domain.Customer;

import java.time.Instant;

public record CustomerDto(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        Instant createdAt,
        long siteCount) {

    public static CustomerDto from(Customer customer, long siteCount) {
        return new CustomerDto(customer.getId(), customer.getName(), customer.getEmail(),
                customer.getPhone(), customer.getAddress(), customer.getCreatedAt(), siteCount);
    }

    public static CustomerDto from(Customer customer) {
        return from(customer, 0);
    }
}