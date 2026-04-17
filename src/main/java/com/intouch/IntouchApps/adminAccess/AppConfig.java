package com.intouch.IntouchApps.adminAccess;

import com.intouch.IntouchApps.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@ToString(exclude = "userRoles")
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "app_config")
public class AppConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String propertyKey;

    @Column(nullable = false, length = 2000)
    private String propertyValue;

    @Column(nullable = false)
    private String propertyType; // STRING or SECRET

    private String keyVersion; // optional, for JWT/Jasypt rotation
}
