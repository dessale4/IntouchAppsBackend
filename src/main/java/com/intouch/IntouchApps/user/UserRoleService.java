package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.role.Role;
import com.intouch.IntouchApps.role.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional
    public void assignRole(Integer userId, Integer roleId, String assignedBy) {
        boolean alreadyAssigned = userRoleRepository
                .findByUserIdAndRoleIdAndActiveTrue(userId, roleId)
                .isPresent();

        if (alreadyAssigned) {
            throw new IllegalStateException("Role is already assigned to the user.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .assignedBy(assignedBy)
                .assignedAt(Instant.now())
                .active(true)
                .build();

        userRoleRepository.save(userRole);
    }

    @Transactional
    public void removeRole(Integer userId, Integer roleId, String removedBy) {
        UserRole userRole = userRoleRepository.findByUserIdAndRoleIdAndActiveTrue(userId, roleId)
                .orElseThrow(() -> new RuntimeException("Active role assignment not found"));

        userRole.setActive(false);
        userRole.setRemovedBy(removedBy);
        userRole.setRemovedAt(Instant.now());
    }

    @Transactional(readOnly = true)
    public List<UserRoleResponse> getActiveRolesByUser(Integer userId) {
        List<UserRole> assignments = userRoleRepository.findByUserIdAndActiveTrue(userId);

        return assignments.stream()
                .map(userRole -> UserRoleResponse.builder()
                        .userId(userRole.getUser().getId())
                        .username(userRole.getUser().getUserName())
                        .roleId(userRole.getRole().getId())
                        .roleName(userRole.getRole().getName())
                        .assignedBy(userRole.getAssignedBy())
                        .assignedAt(userRole.getAssignedAt())
                        .build())
                .toList();
    }

    @Transactional
    public void deleteRole(Integer roleId) {
        boolean hasActiveAssignments = !userRoleRepository.findByRoleIdAndActiveTrue(roleId).isEmpty();

        if (hasActiveAssignments) {
            throw new IllegalStateException("Cannot delete role because it is still assigned to users.");
        }
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        roleRepository.delete(role);
    }
}