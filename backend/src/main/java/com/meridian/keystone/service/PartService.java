package com.meridian.keystone.service;

import com.meridian.keystone.domain.Part;
import com.meridian.keystone.dto.PagedResponse;
import com.meridian.keystone.dto.PartDto;
import com.meridian.keystone.dto.PartRequest;
import com.meridian.keystone.exception.ApiException;
import com.meridian.keystone.repository.PartRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartService {

    private final PartRepository partRepository;

    public PartService(PartRepository partRepository) {
        this.partRepository = partRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<PartDto> list(String search, Pageable pageable) {
        Page<Part> page = (search == null || search.isBlank())
                ? partRepository.findAll(pageable)
                : partRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
                        search.trim(), search.trim(), pageable);
        return PagedResponse.from(page, PartDto::from);
    }

    @Transactional
    public PartDto create(PartRequest request) {
        if (partRepository.existsByCode(request.code().trim())) {
            throw new ApiException(HttpStatus.CONFLICT, "A part with this code already exists");
        }
        Part part = new Part();
        part.setCode(request.code().trim());
        part.setName(request.name().trim());
        part.setUnitCost(request.unitCost());
        part.setStockQty(request.stockQty());
        return PartDto.from(partRepository.save(part));
    }

    @Transactional
    public PartDto update(Long id, PartRequest request) {
        Part part = partRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Part not found"));
        partRepository.findByCode(request.code().trim()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ApiException(HttpStatus.CONFLICT, "A part with this code already exists");
            }
        });
        part.setCode(request.code().trim());
        part.setName(request.name().trim());
        part.setUnitCost(request.unitCost());
        part.setStockQty(request.stockQty());
        return PartDto.from(partRepository.save(part));
    }

    public Part getEntity(Long id) {
        return partRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Part not found"));
    }
}