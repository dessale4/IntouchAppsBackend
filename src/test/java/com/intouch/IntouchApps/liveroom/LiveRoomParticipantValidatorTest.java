package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.user.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LiveRoomParticipantValidatorTest {

    private static final Integer USER_ID = 42;
    private static final Long REQUESTED_ROOM_ID = 100L;

    @Test
    void joinedParticipantInDraftBlocksJoiningAnotherRoom() {
        assertOtherRoomParticipationBlocksJoin();
    }

    @Test
    void joinedParticipantInReadyBlocksJoiningAnotherRoom() {
        assertOtherRoomParticipationBlocksJoin();
    }

    @Test
    void activeParticipantInStartedBlocksJoiningAnotherRoom() {
        assertOtherRoomParticipationBlocksJoin();
    }

    @Test
    void activeParticipantInPausedBlocksJoiningAnotherRoom() {
        assertOtherRoomParticipationBlocksJoin();
    }

    @Test
    void leftParticipantDoesNotBlockJoin() {
        assertNoBlockingParticipationAllowsJoin();
    }

    @Test
    void completedRoomDoesNotBlockJoin() {
        assertNoBlockingParticipationAllowsJoin();
    }

    @Test
    void cancelledRoomDoesNotBlockJoin() {
        assertNoBlockingParticipationAllowsJoin();
    }

    @Test
    void rejoiningTheSameRoomIsAllowed() {
        assertNoBlockingParticipationAllowsJoin();
    }

    @Test
    void sameUserLeftParticipantReceivesSpecificMessageAndRemainsRejected() {
        LiveRoomParticipantValidator validator = validatorReturning(false);
        InTouchRoomParticipant participant = participant(ParticipantStatus.LEFT, USER_ID);

        assertThatThrownBy(() -> validator.ensureSlotClaimable(participant, USER_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "You already left this room. Ask the room owner to reactivate your participation if you want to rejoin."
                );
        org.assertj.core.api.Assertions.assertThat(participant.getStatus())
                .isEqualTo(ParticipantStatus.LEFT);
    }

    @Test
    void anotherUserClaimingSlotReceivesGenericMessage() {
        LiveRoomParticipantValidator validator = validatorReturning(false);
        InTouchRoomParticipant participant = participant(ParticipantStatus.LEFT, 99);

        assertThatThrownBy(() -> validator.ensureSlotClaimable(participant, USER_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Participant code is already claimed or no longer valid.");
    }

    @Test
    void sameUserJoinedRejoinRemainsAllowed() {
        LiveRoomParticipantValidator validator = validatorReturning(false);

        assertThatCode(() -> validator.ensureSlotClaimable(
                participant(ParticipantStatus.JOINED, USER_ID),
                USER_ID
        )).doesNotThrowAnyException();
    }

    @Test
    void sameUserActiveRejoinRemainsAllowed() {
        LiveRoomParticipantValidator validator = validatorReturning(false);

        assertThatCode(() -> validator.ensureSlotClaimable(
                participant(ParticipantStatus.ACTIVE, USER_ID),
                USER_ID
        )).doesNotThrowAnyException();
    }

    private InTouchRoomParticipant participant(ParticipantStatus status, Integer mobileUserId) {
        return InTouchRoomParticipant.builder()
                .status(status)
                .mobileUser(User.builder().id(mobileUserId).build())
                .build();
    }

    private void assertOtherRoomParticipationBlocksJoin() {
        LiveRoomParticipantValidator validator = validatorReturning(true);

        assertThatThrownBy(() -> validator.ensureUserCanJoin(USER_ID, REQUESTED_ROOM_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You are already participating in another active room.");
    }

    private void assertNoBlockingParticipationAllowsJoin() {
        LiveRoomParticipantValidator validator = validatorReturning(false);

        assertThatCode(() -> validator.ensureUserCanJoin(USER_ID, REQUESTED_ROOM_ID))
                .doesNotThrowAnyException();
    }

    private LiveRoomParticipantValidator validatorReturning(boolean exists) {
        InTouchRoomParticipantRepository repository =
                (InTouchRoomParticipantRepository) Proxy.newProxyInstance(
                        InTouchRoomParticipantRepository.class.getClassLoader(),
                        new Class<?>[]{InTouchRoomParticipantRepository.class},
                        (proxy, method, args) -> {
                            if (method.getName().equals("existsActiveParticipantInOtherActiveRoom")) {
                                org.assertj.core.api.Assertions.assertThat(args)
                                        .containsExactly(USER_ID, REQUESTED_ROOM_ID);
                                return exists;
                            }
                            throw new AssertionError(
                                    "Unexpected repository operation: " + method.getName()
                            );
                        }
                );
        return new LiveRoomParticipantValidator(repository);
    }
}
