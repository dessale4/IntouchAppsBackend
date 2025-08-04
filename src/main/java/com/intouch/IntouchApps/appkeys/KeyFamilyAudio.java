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
@Table(name = "KeyFamilyAudio_TBL")
public class KeyFamilyAudio extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer keyFamilyId;
    private String keyFamilyAudioUrl;
    @Column(unique = true)
    private String keyFamilyAudioFileName;
    private boolean isDefault;
    private String keyOneTimeStamp;
    private String keyTwoTimeStamp;
    private String keyThreeTimeStamp;
    private String keyFourTimeStamp;
    private String keyFiveTimeStamp;
    private String keySixTimeStamp;
    private String keySevenTimeStamp;
}
