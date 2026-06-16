package com.intouch.IntouchApps.adminAccess;

import com.intouch.IntouchApps.user.User;
import com.intouch.IntouchApps.user.UserRepository;
import com.intouch.IntouchApps.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAdminService {
    private final UserRepository userRepository;
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
    @Transactional(readOnly = true)
    public Page<AdminUserSummaryResponse> searchUsersForAdmin(
            String search,
            Pageable pageable
    ) {
        String searchTerm = search.contains("@") ? standardPBEStringEncryptor.encrypt(search) : search;
        return userRepository.searchUsersForAdmin(searchTerm, pageable)
                .map(this::toAdminUserSummary);
    }

    private AdminUserSummaryResponse toAdminUserSummary(User user) {
        List<String> roles = user.getUserRoles().stream()
                .filter(UserRole::isActive)
                .map(userRole -> userRole.getRole().getName())
                .sorted()
                .toList();

        return AdminUserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUserName())
                .email(standardPBEStringEncryptor.decrypt(user.getEmail()))
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.isEnabled())
                .roles(roles)
                .build();
    }
}
