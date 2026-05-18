package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "intouch_room_live_key_family_tbl")
public class InTouchRoomLiveKeyFamily extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The room that owns this key family.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private InTouchRoom room;

    // Example: "ሀ family", "English row 1", "Numbers", etc.
    @Column(name = "family_name", nullable = false, length = 100)
    private String familyName;

    // Row position in the target board.
    @Column(name = "row_index", nullable = false)
    private Integer rowIndex;

    // Tigrinya normal rows usually have 7 columns.
    // Special rows may still use 7, but missing columns simply have no key record.
    @Column(name = "column_count", nullable = false)
    private Integer columnCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "key_type", nullable = false, length = 30)
    private LiveKeyType keyType;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
    @Column(name = "family_code", nullable = false, length = 30)
    private String familyCode;
}