package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.common.BaseEntity;
import com.intouch.IntouchApps.user.User;
import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "intouch_room_participant_tbl",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_room_participant_code",
                        columnNames = {"room_id", "participant_code"}
                ),
                @UniqueConstraint(
                        name = "uk_room_mobile_user",
                        columnNames = {"room_id", "mobile_user_id"}
                )
        }
)
public class InTouchRoomParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private InTouchRoom room;

    @Column(name = "participant_code", nullable = false, length = 4)
    private String participantCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mobile_user_id")
    private User mobileUser;   // nullable until claimed

    @Column(nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus status;
    @Column(name = "claimed_at")
    private Instant claimedAt;
    @Column(name = "activated_at")
    private Instant activatedAt;
    @Column(name = "active_in_room")
    private Boolean activeInRoom = false;
}
