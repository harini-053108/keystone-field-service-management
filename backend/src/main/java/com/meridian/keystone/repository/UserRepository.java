package com.meridian.keystone.repository;

import com.meridian.keystone.domain.Role;
import com.meridian.keystone.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByRoleAndCustomerId(Role role, Long customerId);
}