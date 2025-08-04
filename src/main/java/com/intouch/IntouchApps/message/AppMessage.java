package com.intouch.IntouchApps.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.intouch.IntouchApps.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Blob;
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
@Table(name = "AppMessage_TBL")
//@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
public class AppMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank
    @NotEmpty
    private String sendingEmail;
    private String receivingEmail;
    private String sendingUsername;
    private String receivingUsername;
    @Lob
//    @Size(max = 500)
    @NotBlank
    @NotEmpty
    private String message;
    @ElementCollection
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private List<MessageReply> messageReplies = new ArrayList<>();
//    @CreatedDate
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime createdDate;
////    @LastModifiedDate
//    @Column(nullable = false)
//    private LocalDateTime lastModifiedDate;

    public void addMessageReply(MessageReply messageReply){
        messageReplies.add(messageReply);
    }
}
