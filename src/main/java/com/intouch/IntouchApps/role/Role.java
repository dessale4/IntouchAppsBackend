package com.intouch.IntouchApps.role;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.intouch.IntouchApps.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Roles_TBL")
//@EntityListeners(AuditingEntityListener.class)
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true)
    private String name;
//    @ManyToMany(mappedBy = "roles")
//    @JsonIgnore
//    private List<User> users;
//    @CreatedDate
//    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;
//    @LastModifiedDate
//    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

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
