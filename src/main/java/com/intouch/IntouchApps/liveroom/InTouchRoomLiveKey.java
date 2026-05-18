package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
//@Table(name = "intouch_room_live_key_tbl")
@Table(
        name = "intouch_room_live_key_tbl",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_room_family_target_cell",
                        columnNames = {"room_id", "family_id", "target_row", "target_column"}
                )
        }
)
public class InTouchRoomLiveKey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owner room.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private InTouchRoom room;

    // Key family / row.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private InTouchRoomLiveKeyFamily family;

    // Actual displayed key: ሀ, ሁ, A, B, 1, etc.
    @Column(name = "key_value", nullable = false, length = 30)
    private String keyValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "key_type", nullable = false, length = 30)
    private LiveKeyType keyType;

    // Desired target row.
    @Column(name = "target_row", nullable = false)
    private Integer targetRow;

    // Desired target column.
    @Column(name = "target_column", nullable = false)
    private Integer targetColumn;

    // Stable ordering before shuffle.
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
    private String keyFamilyId;
    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}