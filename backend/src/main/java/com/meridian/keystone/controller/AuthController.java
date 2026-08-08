package com.meridian.keystone.controller;

import com.meridian.keystone.domain.User;
import com.meridian.keystone.dto.AuthResponse;
import com.meridian.keystone.dto.LoginRequest;
import com.meridian.keystone.dto.SiteDto;
import com.meridian.keystone.dto.UserDto;
import com.meridian.keystone.security.CurrentUser;
import com.meridian.keystone.service.AuthService;
import com.meridian.keystone.service.SiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and current-user profile")
public class AuthController {

    private final AuthService authService;
    private final SiteService siteService;

    public AuthController(AuthService authService, SiteService siteService) {
        this.authService = authService;
        this.siteService = siteService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive a JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Profile of the authenticated user and, for customers, their sites")
    public MeResponse me(@CurrentUser User user) {
        return new MeResponse(UserDto.from(user), listSitesFor(user));
    }

    private List<SiteDto> listSitesFor(User user) {
        if (user.getCustomer() == null) {
            return List.of();
        }
        return siteService.listByCustomer(user.getCustomer().getId());
    }

    public record MeResponse(UserDto user, List<SiteDto> sites) {
    }
}