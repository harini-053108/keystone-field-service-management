package com.meridian.keystone.dto;

import com.meridian.keystone.domain.Site;

import java.time.Instant;

public record SiteDto(
        Long id,
        String name,
        String address,
        Long customerId,
        Instant createdAt) {

    public static SiteDto from(Site site) {
        Long customerId = site.getCustomer() != null ? site.getCustomer().getId() : null;
        return new SiteDto(site.getId(), site.getName(), site.getAddress(), customerId, site.getCreatedAt());
    }
}