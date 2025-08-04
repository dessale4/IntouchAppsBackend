package com.intouch.IntouchApps.appkeys;

import com.intouch.IntouchApps.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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

    private boolean isDefault;
    private String keyAudioTimeStamp;
}
