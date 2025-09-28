package com.intouch.IntouchApps.appkeys;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intouch.IntouchApps.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

//@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "KeyAudio_TBL")
public class KeyAudio extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer keyFamilyId;
    private Integer keyId;
    private String keyAudioUrl;
    @Column(unique = true)
    private String keyAudioFileName;
    @JsonProperty("isDefault") // For JSON serialization/deserialization
    @Column(name = "is_default") // For JPA column mapping
    private boolean isDefault;
    //keyAudioTimeStamp is used in cases where keyAudio is played from defaultKeyFamilyAudio
    private String keyAudioTimeStamp;
}
