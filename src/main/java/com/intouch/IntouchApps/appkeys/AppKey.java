package com.intouch.IntouchApps.appkeys;

import com.intouch.IntouchApps.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.*;

//@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "AppKey_TBL")
//@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = true)
public class AppKey extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer keyId;
    private Integer keyFamilyId;
    @Column(unique = true)
    private String keyName;
    private String keyNameInEnglish;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<KeyExample> keyExamples = new HashSet<>();
//    @OneToOne(cascade = CascadeType.ALL)
//    private KeyExample defaultKeyExample;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<KeyAudio> keyAudios = new HashSet<>();
//    @OneToOne(cascade = CascadeType.ALL)
//    private KeyAudio defaultKeyAudio;
    public void addKeyExample(KeyExample keyExample){
        keyExamples.add(keyExample);
    }
    public void addKeyAudio(KeyAudio keyAudio){
        keyAudios.add(keyAudio);
    }



}
