package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "intouch_room_group_tbl")
public class InTouchRoomGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private InTouchRoom room;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer sortOrder;
    @Builder.Default
    @Column(name = "error_count", nullable = false)
    private Integer errorCount = 0;

    @Builder.Default
    @Column(name = "completed_key_count", nullable = false)
    private Integer completedKeyCount = 0;

    @Column(name = "completed_at")
    private Instant completedAt;
//    @Column(name = "is_completed")
//    @Builder.Default
//    private boolean completed = false;
    @Builder.Default
    @Column(name = "score", nullable = false)
    private Integer score = 0;

    @Builder.Default
    @Column(name = "completed_pattern_count", nullable = false)
    private Integer completedPatternCount = 0;
}
