package com.intouch.IntouchApps.liveroom.entity;

import com.intouch.IntouchApps.liveroom.InTouchRoom;
import com.intouch.IntouchApps.liveroom.InTouchRoomGroup;
import com.intouch.IntouchApps.liveroom.InTouchRoomParticipant;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "intouch_room_group_board_row_tbl",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_room_group_board_row",
                        columnNames = {"room_id", "group_id", "row_index"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_group_board_room_group",
                        columnList = "room_id, group_id"
                )
        }
)
public class InTouchRoomGroupBoardRow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private InTouchRoom room;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private InTouchRoomGroup group;

    @Column(name = "row_index", nullable = false)
    private Integer rowIndex;

    @Column(name = "key_family_id", length = 50)
    private String keyFamilyId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_participant_id")
    private InTouchRoomParticipant createdByParticipant;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}