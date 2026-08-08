package com.meridian.keystone.controller;

import com.meridian.keystone.domain.Role;
import com.meridian.keystone.domain.User;
import com.meridian.keystone.dto.SiteDto;
import com.meridian.keystone.dto.SiteRequest;
import com.meridian.keystone.exception.ApiException;
import com.meridian.keystone.security.CurrentUser;
import com.meridian.keystone.service.SiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers/{customerId}/sites")
@Tag(name = "Sites", description = "Sites belonging to a customer")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    @Operation(summary = "List sites for a customer (customers scoped to their own organisation)")
    public List<SiteDto> list(@PathVariable Long customerId, @CurrentUser User actor) {
        if (actor.getRole() == Role.CUSTOMER
                && (actor.getCustomer() == null || !actor.getCustomer().getId().equals(customerId))) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Customer not found");
        }
        return siteService.listByCustomer(customerId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a site")
    public SiteDto create(@PathVariable Long customerId, @Valid @RequestBody SiteRequest request) {
        return siteService.create(customerId, request);
    }

    @PutMapping("/{siteId}")
    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    @Operation(summary = "Update a site")
    public SiteDto update(@PathVariable Long customerId, @PathVariable Long siteId,
                          @Valid @RequestBody SiteRequest request) {
        return siteService.update(customerId, siteId, request);
    }

    @DeleteMapping("/{siteId}")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a site (manager only)")
    public void delete(@PathVariable Long customerId, @PathVariable Long siteId) {
        siteService.delete(customerId, siteId);
    }
}