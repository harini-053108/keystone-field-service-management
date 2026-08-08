package com.meridian.keystone.security;

import com.meridian.keystone.domain.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** Access to the currently authenticated principal from the service layer. */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<UserPrincipal> currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static UserPrincipal requirePrincipal() {
        return currentPrincipal().orElseThrow();
    }

    public static Long currentUserId() {
        return requirePrincipal().getId();
    }

    public static boolean hasRole(Role role) {
        return currentPrincipal().map(p -> p.getRole() == role).orElse(false);
    }
}