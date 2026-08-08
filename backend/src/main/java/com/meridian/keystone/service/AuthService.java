package com.meridian.keystone.service;

import com.meridian.keystone.domain.User;
import com.meridian.keystone.dto.AuthResponse;
import com.meridian.keystone.dto.LoginRequest;
import com.meridian.keystone.exception.ApiException;
import com.meridian.keystone.repository.UserRepository;
import com.meridian.keystone.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        String token = jwtService.generateToken(user);
        Long customerId = user.getCustomer() != null ? user.getCustomer().getId() : null;
        return new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), user.getFullName(),
                user.getRole(), customerId);
    }
}