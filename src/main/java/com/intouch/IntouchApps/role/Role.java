package com.intouch.IntouchApps.role;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intouch.IntouchApps.user.User;
import com.intouch.IntouchApps.user.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "userRoles")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "Roles_TBL")
//@EntityListeners(AuditingEntityListener.class)
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;
    @Column(nullable = false, unique = true)
    private String name;
    @OneToMany(mappedBy = "role")
    @Builder.Default
    @JsonIgnore//Prevents cyclic dependence during object serialization using @ResponseBody in an api return
    private Set<UserRole> userRoles = new HashSet<>();
    private Instant createdDate;
    private Instant lastModifiedDate;

    @Override
    public String toString() {
        return "Role{" +
                "name='" + name + '\'' +
                ", createdDate=" + createdDate +
                ", lastModifiedDate=" + lastModifiedDate +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return getName().equals(role.getName());
    }
    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
