package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.*;

@Entity

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "intouch_room_group_participant_tbl",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_room_participant_one_group",
                        columnNames = {"room_id", "participant_id"}
                )
        }
)
public class InTouchRoomGroupParticipant extends BaseEntity {

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
    @JoinColumn(name = "participant_id", nullable = false)
    private InTouchRoomParticipant participant;
}
