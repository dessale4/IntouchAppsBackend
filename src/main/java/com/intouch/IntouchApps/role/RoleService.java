package com.intouch.IntouchApps.role;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        String normalizedName = normalizeRoleName(request.getName());

        if (roleRepository.existsByName(normalizedName)) {
            throw new IllegalStateException("Role already exists: " + normalizedName);
        }

        Role role = Role.builder()
                .name(normalizedName)
                .build();

        Role savedRole = roleRepository.save(role);

        return RoleResponse.builder()
                .id(savedRole.getId())
                .name(savedRole.getName())
                .build();
    }
    private String normalizeRoleName(String roleName) {
        String trimmed = roleName.trim().toUpperCase();
        return trimmed.startsWith("ROLE_") ? trimmed : "ROLE_" + trimmed;
    }
}