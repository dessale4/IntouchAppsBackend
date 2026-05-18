package com.intouch.IntouchApps.liveroom.withPattern;

import com.intouch.IntouchApps.common.BaseEntity;
import com.intouch.IntouchApps.liveroom.InTouchRoom;
import com.intouch.IntouchApps.liveroom.InTouchRoomGroup;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "intouch_room_group_pattern_progress_tbl",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_pattern_progress",
                        columnNames = {"group_id", "pattern_id"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InTouchRoomGroupPatternProgress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private InTouchRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private InTouchRoomGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pattern_id", nullable = false)
    private InTouchRoomBoardPattern pattern;

    @Builder.Default
    @Column(nullable = false)
    private Boolean completed = false;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Builder.Default
    @Column(name = "earned_points", nullable = false)
    private Integer earnedPoints = 0;
}
