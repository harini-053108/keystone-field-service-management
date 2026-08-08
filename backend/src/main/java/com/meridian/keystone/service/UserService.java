package com.meridian.keystone.service;

import com.meridian.keystone.domain.Customer;
import com.meridian.keystone.domain.Role;
import com.meridian.keystone.domain.User;
import com.meridian.keystone.dto.PagedResponse;
import com.meridian.keystone.dto.UserDto;
import com.meridian.keystone.dto.UserRequest;
import com.meridian.keystone.exception.ApiException;
import com.meridian.keystone.repository.CustomerRepository;
import com.meridian.keystone.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, CustomerRepository customerRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserDto> list(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return PagedResponse.from(page, UserDto::from);
    }

    @Transactional(readOnly = true)
    public List<UserDto> listByRole(Role role) {
        return userRepository.findByRole(role).stream().map(UserDto::from).toList();
    }

    @Transactional
    public UserDto create(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "A user with this email already exists");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password is required for new users");
        }
        User user = new User();
        apply(user, request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    public UserDto update(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        userRepository.findByEmail(request.email()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ApiException(HttpStatus.CONFLICT, "A user with this email already exists");
            }
        });
        apply(user, request);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return UserDto.from(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        userRepository.delete(user);
    }

    private void apply(User user, UserRequest request) {
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim());
        user.setRole(request.role());
        if (request.role() == Role.CUSTOMER) {
            if (request.customerId() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "A customer link is required for CUSTOMER users");
            }
            Customer customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found"));
            user.setCustomer(customer);
        } else {
            user.setCustomer(null);
        }
    }
}