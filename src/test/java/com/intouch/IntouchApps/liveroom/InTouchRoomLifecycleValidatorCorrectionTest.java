package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.user.User;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InTouchRoomLifecycleValidatorCorrectionTest {

    private final InTouchRoomLifecycleValidator validator = new InTouchRoomLifecycleValidator();

    @ParameterizedTest
    @EnumSource(InTouchRoomStatus.class)
    void participantClaimReleaseIsAllowedOnlyBeforeStart(InTouchRoomStatus status) {
        InTouchRoom room = InTouchRoom.builder().status(status).deleted(false).build();
        InTouchRoomParticipant participant = claimedParticipant();

        if (status == InTouchRoomStatus.DRAFT || status == InTouchRoomStatus.READY) {
            assertThatCode(() -> validator.ensureCanReleaseParticipantClaim(room, participant))
                    .doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> validator.ensureCanReleaseParticipantClaim(room, participant))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @ParameterizedTest
    @EnumSource(value = InTouchRoomStatus.class, names = {"DRAFT", "READY"})
    void unclaimedParticipantStillUsesExistingValidation(InTouchRoomStatus status) {
        InTouchRoom room = InTouchRoom.builder().status(status).deleted(false).build();

        assertThatThrownBy(() -> validator.ensureCanReleaseParticipantClaim(
                room, InTouchRoomParticipant.builder().build()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Participant is not currently claimed.");
    }

    @ParameterizedTest
    @EnumSource(value = InTouchRoomStatus.class, names = {"DRAFT", "READY"})
    void deletedFlagRejectsParticipantClaimRelease(InTouchRoomStatus status) {
        InTouchRoom room = InTouchRoom.builder().status(status).deleted(true).build();

        assertThatThrownBy(() -> validator.ensureCanReleaseParticipantClaim(
                room, claimedParticipant()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Deleted room cannot release participant claims.");
    }

    private InTouchRoomParticipant claimedParticipant() {
        return InTouchRoomParticipant.builder()
                .mobileUser(User.builder().id(1).build())
                .build();
    }
}
