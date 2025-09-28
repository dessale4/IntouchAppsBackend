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
@Table(name = "KeyFamilyAudio_TBL")
public class KeyFamilyAudio extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer keyFamilyId;
    private String keyFamilyAudioUrl;
    @Column(unique = true)
    private String keyFamilyAudioFileName;
    @JsonProperty("isDefault") // For JSON serialization/deserialization
    @Column(name = "is_default") // For JPA column mapping
    private boolean isDefault;
    private String keyOneTimeStamp;
    private String keyTwoTimeStamp;
    private String keyThreeTimeStamp;
    private String keyFourTimeStamp;
    private String keyFiveTimeStamp;
    private String keySixTimeStamp;
    private String keySevenTimeStamp;
}
