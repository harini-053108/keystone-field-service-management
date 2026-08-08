package com.meridian.keystone.service;

import com.meridian.keystone.domain.Site;
import com.meridian.keystone.dto.PagedResponse;
import com.meridian.keystone.dto.SiteDto;
import com.meridian.keystone.dto.SiteRequest;
import com.meridian.keystone.exception.ApiException;
import com.meridian.keystone.repository.CustomerRepository;
import com.meridian.keystone.repository.SiteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final CustomerRepository customerRepository;

    public SiteService(SiteRepository siteRepository, CustomerRepository customerRepository) {
        this.siteRepository = siteRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public List<SiteDto> listByCustomer(Long customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found"));
        return siteRepository.findByCustomerId(customerId).stream().map(SiteDto::from).toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<SiteDto> listByCustomerPaged(Long customerId, Pageable pageable) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found"));
        Page<Site> page = siteRepository.findByCustomerId(customerId, pageable);
        return PagedResponse.from(page, SiteDto::from);
    }

    @Transactional
    public SiteDto create(Long customerId, SiteRequest request) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found"));
        Site site = new Site();
        site.setName(request.name().trim());
        site.setAddress(request.address());
        site.setCustomer(customer);
        return SiteDto.from(siteRepository.save(site));
    }

    @Transactional
    public SiteDto update(Long customerId, Long siteId, SiteRequest request) {
        Site site = getEntity(siteId);
        if (!site.getCustomer().getId().equals(customerId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Site not found for this customer");
        }
        site.setName(request.name().trim());
        site.setAddress(request.address());
        return SiteDto.from(siteRepository.save(site));
    }

    @Transactional
    public void delete(Long customerId, Long siteId) {
        Site site = getEntity(siteId);
        if (!site.getCustomer().getId().equals(customerId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Site not found for this customer");
        }
        siteRepository.delete(site);
    }

    private Site getEntity(Long siteId) {
        return siteRepository.findById(siteId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Site not found"));
    }
}