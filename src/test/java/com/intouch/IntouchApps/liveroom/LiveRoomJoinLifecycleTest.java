package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.user.User;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LiveRoomJoinLifecycleTest {

    private static final Integer USER_ID = 42;
    private final LiveRoomParticipantValidator validator =
            new LiveRoomParticipantValidator(null);

    @ParameterizedTest
    @EnumSource(InTouchRoomStatus.class)
    void invitedClaimsAreAllowedOnlyInDraftOrReady(InTouchRoomStatus roomStatus) {
        InTouchRoomParticipant participant = participant(
                ParticipantStatus.INVITED, roomStatus, null, false);

        if (roomStatus == InTouchRoomStatus.DRAFT || roomStatus == InTouchRoomStatus.READY) {
            assertThatCode(() -> validator.ensureRoomAllowsJoin(participant, USER_ID))
                    .doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> validator.ensureRoomAllowsJoin(participant, USER_ID))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @ParameterizedTest
    @EnumSource(InTouchRoomStatus.class)
    void joinedReentryIsAllowedOnlyInDraftOrReady(InTouchRoomStatus roomStatus) {
        assertStateMatrix(ParticipantStatus.JOINED, roomStatus,
                roomStatus == InTouchRoomStatus.DRAFT || roomStatus == InTouchRoomStatus.READY);
    }

    @ParameterizedTest
    @EnumSource(InTouchRoomStatus.class)
    void activeReentryIsAllowedOnlyInStartedOrPaused(InTouchRoomStatus roomStatus) {
        assertStateMatrix(ParticipantStatus.ACTIVE, roomStatus,
                roomStatus == InTouchRoomStatus.STARTED || roomStatus == InTouchRoomStatus.PAUSED);
    }

    @ParameterizedTest
    @EnumSource(value = ParticipantStatus.class, names = {"INVITED", "JOINED", "ACTIVE"})
    void deletedFlagAlwaysRejects(ParticipantStatus status) {
        InTouchRoomParticipant participant = participant(status, InTouchRoomStatus.DRAFT,
                status == ParticipantStatus.INVITED ? null : USER_ID, true);

        assertThatThrownBy(() -> validator.ensureRoomAllowsJoin(participant, USER_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Deleted room cannot be joined.");
    }

    @Test
    void sameUserLeftKeepsOwnerReactivationMessage() {
        InTouchRoomParticipant participant = participant(
                ParticipantStatus.LEFT, InTouchRoomStatus.COMPLETED, USER_ID, false);

        assertThatCode(() -> validator.ensureRoomAllowsJoin(participant, USER_ID))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> validator.ensureSlotClaimable(participant, USER_ID))
                .hasMessage(
                        "You already left this room. Ask the room owner to reactivate your participation if you want to rejoin."
                );
    }

    private void assertStateMatrix(
            ParticipantStatus status,
            InTouchRoomStatus roomStatus,
            boolean allowed
    ) {
        InTouchRoomParticipant participant = participant(status, roomStatus, USER_ID, false);
        if (allowed) {
            assertThatCode(() -> validator.ensureRoomAllowsJoin(participant, USER_ID))
                    .doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> validator.ensureRoomAllowsJoin(participant, USER_ID))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    private InTouchRoomParticipant participant(
            ParticipantStatus status,
            InTouchRoomStatus roomStatus,
            Integer mobileUserId,
            boolean deleted
    ) {
        InTouchRoom room = InTouchRoom.builder()
                .id(1L)
                .status(roomStatus)
                .deleted(deleted)
                .build();
        return InTouchRoomParticipant.builder()
                .room(room)
                .status(status)
                .mobileUser(mobileUserId == null ? null : User.builder().id(mobileUserId).build())
                .build();
    }
}
