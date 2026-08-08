package com.meridian.keystone.controller;

import com.meridian.keystone.domain.Role;
import com.meridian.keystone.dto.PagedResponse;
import com.meridian.keystone.dto.UserDto;
import com.meridian.keystone.dto.UserRequest;
import com.meridian.keystone.service.UserService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management (manager only)")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "List users")
    public PagedResponse<UserDto> list(@PageableDefault(size = 50) Pageable pageable) {
        return userService.list(pageable);
    }

    @GetMapping("/technicians")
    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    @Operation(summary = "List technicians (for dispatch)")
    public List<UserDto> technicians() {
        return userService.listByRole(Role.TECHNICIAN);
    }

    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a user")
    public UserDto create(@Valid @RequestBody UserRequest request) {
        return userService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Update a user")
    public UserDto update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a user")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}