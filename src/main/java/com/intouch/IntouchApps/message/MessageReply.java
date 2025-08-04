package com.intouch.IntouchApps.message;

import com.intouch.IntouchApps.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//@Getter
//@Setter
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
//    @Size(max = 500)
    @NotBlank
    @NotEmpty
    private String message;
}
