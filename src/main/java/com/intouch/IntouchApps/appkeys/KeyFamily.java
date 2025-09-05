package com.intouch.IntouchApps.appkeys;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

//@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "KeyFamily_TBL")
public class KeyFamily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(unique = true)
    private Integer keyFamilyId;
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<KeyFamilyAudio> keyFamilyAudioSet = new HashSet<>();
    @OneToOne(cascade = CascadeType.ALL)
    private KeyFamilyAudio defaultKeyFamilyAudio;
    @OneToOne(cascade = CascadeType.ALL)
    private AppKey keyOne;
    @OneToOne(cascade = CascadeType.ALL)
    private AppKey keyTwo;
    @OneToOne(cascade = CascadeType.ALL)
    private AppKey keyThree;
    @OneToOne(cascade = CascadeType.ALL)
    private AppKey keyFour;
    @OneToOne(cascade = CascadeType.ALL)
    private AppKey keyFive;
    @OneToOne(cascade = CascadeType.ALL)
    private AppKey keySix;
    @OneToOne(cascade = CascadeType.ALL)
    private AppKey keySeven;

    public void addKeyFamilyAudio(KeyFamilyAudio keyFamilyAudio) {
        this.keyFamilyAudioSet.add(keyFamilyAudio);
    }
}
