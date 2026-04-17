package com.intouch.IntouchApps.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user-roles")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> assignRole(
            @Valid @RequestBody AssignRoleRequest request,
            Principal principal
    ) {
        String assignedBy = principal.getName(); // current logged-in admin username
        userRoleService.assignRole(request.getUserId(), request.getRoleId(), assignedBy);

        return ResponseEntity.ok(Map.of("message", "Role assigned successfully"));
    }

    @PutMapping("/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> removeRole(
            @Valid @RequestBody RemoveRoleRequest request,
            Principal principal
    ) {
        String removedBy = principal.getName();
        userRoleService.removeRole(request.getUserId(), request.getRoleId(), removedBy);

        return ResponseEntity.ok(Map.of("message", "Role removed successfully"));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserRoleResponse>> getActiveRolesByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(userRoleService.getActiveRolesByUser(userId));
    }
}
