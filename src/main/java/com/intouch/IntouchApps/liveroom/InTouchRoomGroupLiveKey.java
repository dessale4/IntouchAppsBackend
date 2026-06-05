package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "intouch_room_group_live_key_tbl",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_current_cell",
                        columnNames = {"room_id", "group_id", "current_row", "current_column"}
                )
        }
)
public class InTouchRoomGroupLiveKey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Room reference for faster queries.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private InTouchRoom room;

    // Group that owns this copy.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private InTouchRoomGroup group;

    // Original template key.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_live_key_id", nullable = false)
    private InTouchRoomLiveKey sourceLiveKey;

    // Participant assigned to work on this key.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_participant_id")
    private InTouchRoomParticipant assignedParticipant;

    @Column(name = "key_value", nullable = false, length = 30)
    private String keyValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "key_type", nullable = false, length = 30)
    private LiveKeyType keyType;

    // Shuffled assignment order.
    @Column(name = "assigned_order")
    private Integer assignedOrder;

    // Current board location selected by participant.
    @Column(name = "current_row")
    private Integer currentRow;

    @Column(name = "current_column")
    private Integer currentColumn;

    // Winning target position.
    @Column(name = "target_row", nullable = false)
    private Integer targetRow;

    @Column(name = "target_column", nullable = false)
    private Integer targetColumn;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LiveKeyBuildStatus status;

    @Version
    private Long version;
    private String keyFamilyId;
    private Instant placedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "removed_by_participant_id")
    private InTouchRoomParticipant removedByParticipant;
    private Instant removedAt;
}