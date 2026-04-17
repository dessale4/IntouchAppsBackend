package com.intouch.IntouchApps.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    Optional<UserRole> findByUserIdAndRoleIdAndActiveTrue(Integer userId, Integer roleId);

    List<UserRole> findByUserIdAndActiveTrue(Integer userId);

    List<UserRole> findByRoleIdAndActiveTrue(Integer roleId);
}
