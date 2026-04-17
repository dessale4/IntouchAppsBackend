package com.intouch.IntouchApps.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intouch.IntouchApps.role.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "user_roles_tbl",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_role_active", columnNames = {"user_id", "role_id", "active"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "role"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore//Prevents cyclic dependence during object serialization using @ResponseBody in an api return
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
//    @JsonIgnore//Prevents cyclic dependence during object serialization using @ResponseBody in an api return
    private Role role;
    @Column(nullable = false, length = 100)
    private String assignedBy;
    @Column(nullable = false)
    private Instant assignedAt;
    @Column(length = 100)
    private String removedBy;
    private Instant removedAt;
    @Column(nullable = false)
    private boolean active = true;
}
