package com.intouch.IntouchApps.appkeys;


import com.fasterxml.jackson.annotation.JsonProperty;
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
@Table(name = "KeyExample_TBL")
public class KeyExample extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String englishName;
    @Column(unique = true)
    private String tigrinyaName;
    private String amharicName;
    private String category;
    private Integer keyId;
    private Integer keyFamilyId;
    @JsonProperty
    private boolean isDefault;
    private String imageUrl;
    @Column(unique = true)
    private String imageFileName;
    @Column(unique = true)
    private String audioFileName;
    private String audioUrl;
}
