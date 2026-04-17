package com.intouch.IntouchApps.message;

import com.intouch.IntouchApps.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@ToString(exclude = "userRoles")
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "MessageReply_TBL")
//@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
public class MessageReply  extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank
    @NotEmpty
    private String sendingEmail;
    @NotBlank
    @NotEmpty
    private String sendingUsername;
    @Lob
    @Size(max = 500)
    @NotBlank
//    @NotEmpty
    private String message;
}
