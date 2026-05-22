package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.adminAccess.AdminUserSummaryResponse;
import com.intouch.IntouchApps.constants.RoleConstants;
import com.intouch.IntouchApps.role.Role;
import com.intouch.IntouchApps.role.RoleRepository;
import com.intouch.IntouchApps.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final SecurityUtils securityUtils;
    private static final Set<String> ASSIGNABLE_ROLES = Set.of(
            RoleConstants.ROLE_LIVEROOM_OWNER
    );

    @Transactional
    public void assignRole(AssignRoleRequest request) {
        String currentUsername = securityUtils.getCurrentUsername();

        String roleName = request.getRoleName().trim().toUpperCase();

        if (!ASSIGNABLE_ROLES.contains(roleName)) {
            throw new IllegalArgumentException(
                    "This role cannot be assigned from admin UI."
            );
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found."));

        boolean alreadyAssigned = userRoleRepository
                .findByUserIdAndRoleIdAndActiveTrue(user.getId(), role.getId())
                .isPresent();

        if (alreadyAssigned) {
            throw new IllegalStateException("Role is already assigned to the user.");
        }

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .assignedBy(currentUsername)
                .assignedAt(Instant.now())
                .active(true)
                .build();

        userRoleRepository.save(userRole);
    }

    @Transactional
    public void removeRole(RemoveRoleRequest request) {
        String currentUsername = securityUtils.getCurrentUsername();

        String roleName = request.getRoleName().trim().toUpperCase();

        if (!ASSIGNABLE_ROLES.contains(roleName)) {
            throw new IllegalArgumentException(
                    "This role cannot be removed from admin UI."
            );
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found."));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        UserRole userRole = userRoleRepository
                .findByUserIdAndRoleIdAndActiveTrue(user.getId(), role.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Active role assignment not found."
                ));

        userRole.setActive(false);
        userRole.setRemovedBy(currentUsername);
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