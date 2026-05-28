package com.intouch.IntouchApps.liveroom.withPattern;

import com.intouch.IntouchApps.common.BaseEntity;
import com.intouch.IntouchApps.liveroom.InTouchRoom;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "intouch_room_board_pattern_tbl")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InTouchRoomBoardPattern extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private InTouchRoom room;

    @Column(nullable = false, length = 100)
    private String name; // "Row 1", "California Pattern", "Main Diagonal"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LiveRoomPatternType patternType;

    @Column(nullable = false)
    private Integer points;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
    @Builder.Default
    @Column(name = "key_family_key", length = 50, nullable = false)
    private String keyFamilyKey = "";
}
