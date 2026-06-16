package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.common.BaseEntity;
import com.intouch.IntouchApps.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@ToString
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "intouch_room_tbl",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_intouch_room_code",
                        columnNames = "room_code"
                )
        }
)
public class InTouchRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Six-character alpha numeric code, for example A7K9P2.
    @Column(name = "room_code", nullable = false, length = 6, unique = true)
    private String roomCode;

    @Column(nullable = false, length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "build_mode", nullable = false, length = 40)
    private LiveRoomBuildMode buildMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InTouchRoomStatus status;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "placement_strategy", nullable = false, length = 40)
    private LiveKeyPlacementStrategy placementStrategy =
            LiveKeyPlacementStrategy.EXACT_TARGET_POSITION;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    // Optional JSON target description for custom/mixed structures.
    @Column(name = "target_structure", columnDefinition = "TEXT")
    private String targetStructure;

    @Builder.Default
    @Column(name = "shuffle_keys", nullable = false)
    private Boolean shuffleKeys = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;
    @Builder.Default
    @Column(name = "scoring_enabled", nullable = false)
    private Boolean scoringEnabled = false;
    private Instant startedAt;
    private Instant completedAt;
    @Column(name = "paid_room", nullable = false)
    @Builder.Default
    private Boolean paidRoom = true;

    @Column(name = "replay_count", nullable = false)
    @Builder.Default
    private Integer replayCount = 0;
}