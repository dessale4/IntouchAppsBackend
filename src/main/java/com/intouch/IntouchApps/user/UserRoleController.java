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
            @Valid @RequestBody AssignRoleRequest assignRoleRequest
    ) {
        userRoleService.assignRole(assignRoleRequest);

        return ResponseEntity.ok(Map.of("message", "Role assigned successfully"));
    }

    @PutMapping("/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> removeRole(
            @Valid @RequestBody RemoveRoleRequest removeRoleRequest
    ) {
        userRoleService.removeRole(removeRoleRequest);

        return ResponseEntity.ok(Map.of("message", "Role removed successfully"));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserRoleResponse>> getActiveRolesByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(userRoleService.getActiveRolesByUser(userId));
    }
    public ResponseEntity<?> getAllUserRoles(){
//        users.stream()
//                .flatMap(user -> user.getRoles().stream())
        return null;
    }
}
