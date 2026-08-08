package com.meridian.keystone.repository;

import com.meridian.keystone.domain.Part;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartRepository extends JpaRepository<Part, Long> {

    boolean existsByCode(String code);

    Optional<Part> findByCode(String code);

    Page<Part> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(String code, String name, Pageable pageable);

    Page<Part> findAll(Pageable pageable);
}