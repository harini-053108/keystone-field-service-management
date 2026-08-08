package com.meridian.keystone.controller;

import com.meridian.keystone.dto.PagedResponse;
import com.meridian.keystone.dto.PartDto;
import com.meridian.keystone.dto.PartRequest;
import com.meridian.keystone.service.PartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/parts")
@Tag(name = "Parts", description = "Inventory parts")
public class PartController {

    private final PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }

    @GetMapping
    @Operation(summary = "List parts (searchable, paginated) — available to any logged-in user")
    public PagedResponse<PartDto> list(@RequestParam(required = false) String search,
                                       @PageableDefault(size = 50) Pageable pageable) {
        return partService.list(search, pageable);
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a part (manager only)")
    public PartDto create(@Valid @RequestBody PartRequest request) {
        return partService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update a part (manager only)")
    public PartDto update(@PathVariable Long id, @Valid @RequestBody PartRequest request) {
        return partService.update(id, request);
    }
}