package com.intouch.IntouchApps.liveroom.withPattern;

import com.intouch.IntouchApps.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "intouch_room_board_pattern_cell_tbl")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InTouchRoomBoardPatternCell extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pattern_id", nullable = false)
    private InTouchRoomBoardPattern pattern;

    @Column(name = "target_row", nullable = false)
    private Integer targetRow;

    @Column(name = "target_column", nullable = false)
    private Integer targetColumn;

    @Column(name = "expected_value", length = 50)
    private String expectedValue;
}
