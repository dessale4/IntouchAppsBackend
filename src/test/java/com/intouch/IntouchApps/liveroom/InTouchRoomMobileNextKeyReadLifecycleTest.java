package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.security.SecurityUtils;
import com.intouch.IntouchApps.user.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InTouchRoomMobileNextKeyReadLifecycleTest {

    private static final Long ROOM_ID = 10L;
    private static final Integer USER_ID = 42;

    @Test
    void authenticatedParticipantCanReadCompletedRoomStatusWithoutGameplayAccess() {
        Fixture fixture = fixture(
                InTouchRoomStatus.COMPLETED, ParticipantStatus.ACTIVE, true, false);

        var response = fixture.service().getParticipantRoomStatus(ROOM_ID);

        assertThat(response.getRoomStatus()).isEqualTo(InTouchRoomStatus.COMPLETED);
        assertThat(fixture.participantLookups()).isEqualTo(1);
    }

    @Test
    void participantStatusReadRejectsUnassignedAndDeletedRooms() {
        Fixture unassigned = fixture(InTouchRoomStatus.STARTED, null, false, false);
        assertThatThrownBy(() -> unassigned.service().getParticipantRoomStatus(ROOM_ID))
                .hasMessage("You are not assigned to this live room.");

        Fixture deleted = fixture(InTouchRoomStatus.COMPLETED, ParticipantStatus.ACTIVE, true, true);
        assertThatThrownBy(() -> deleted.service().getParticipantRoomStatus(ROOM_ID))
                .hasMessage("Deleted room status is unavailable.");
        assertThat(deleted.participantLookups()).isZero();
    }

    @Test
    void activeParticipantWithActiveFlagCanReadWithoutOwningAKey() {
        Fixture fixture = fixture(InTouchRoomStatus.STARTED, ParticipantStatus.ACTIVE, true, false);

        var response = fixture.service().getNextKeyForCurrentParticipant(ROOM_ID);

        assertThat(response.isNoMoreKeys()).isTrue();
        assertThat(fixture.participantLookups()).isEqualTo(1);
    }

    @Test
    void inactiveLeftAndJoinedParticipantsAreRejected() {
        assertParticipantRejected(ParticipantStatus.ACTIVE, false);
        assertParticipantRejected(ParticipantStatus.LEFT, false);
        assertParticipantRejected(ParticipantStatus.JOINED, false);
    }

    @Test
    void cancelledCompletedPausedAndDeletedRoomsAreRejectedBeforeParticipantRead() {
        for (InTouchRoomStatus status : List.of(
                InTouchRoomStatus.CANCELLED,
                InTouchRoomStatus.COMPLETED,
                InTouchRoomStatus.PAUSED,
                InTouchRoomStatus.DELETED)) {
            Fixture fixture = fixture(status, ParticipantStatus.ACTIVE, true, false);
            assertThatThrownBy(() -> fixture.service().getNextKeyForCurrentParticipant(ROOM_ID))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(fixture.participantLookups()).isZero();
        }

        Fixture deleted = fixture(
                InTouchRoomStatus.STARTED, ParticipantStatus.ACTIVE, true, true);
        assertThatThrownBy(() -> deleted.service().getNextKeyForCurrentParticipant(ROOM_ID))
                .hasMessage("Deleted room cannot be used for gameplay.");
        assertThat(deleted.participantLookups()).isZero();
    }

    @Test
    void missingParticipantUsesExistingAssignmentError() {
        Fixture fixture = fixture(InTouchRoomStatus.STARTED, null, false, false);

        assertThatThrownBy(() -> fixture.service().getNextKeyForCurrentParticipant(ROOM_ID))
                .hasMessage("You are not assigned to this live room.");
    }

    private void assertParticipantRejected(ParticipantStatus status, boolean activeInRoom) {
        Fixture fixture = fixture(InTouchRoomStatus.STARTED, status, activeInRoom, false);
        assertThatThrownBy(() -> fixture.service().getNextKeyForCurrentParticipant(ROOM_ID))
                .hasMessage("Participant is not active in this room.");
    }

    private Fixture fixture(
            InTouchRoomStatus roomStatus,
            ParticipantStatus participantStatus,
            boolean activeInRoom,
            boolean deleted
    ) {
        InTouchRoom room = InTouchRoom.builder()
                .id(ROOM_ID)
                .title("Room")
                .status(roomStatus)
                .buildMode(LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY)
                .deleted(deleted)
                .build();
        InTouchRoomParticipant participant = participantStatus == null ? null :
                InTouchRoomParticipant.builder()
                        .id(20L)
                        .room(room)
                        .mobileUser(User.builder().id(USER_ID).build())
                        .status(participantStatus)
                        .activeInRoom(activeInRoom)
                        .build();

        InTouchRoomRepository roomRepository = proxy(InTouchRoomRepository.class,
                (name, args) -> name.equals("findById")
                        ? Optional.of(room)
                        : unexpected(name));
        int[] participantLookups = {0};
        InTouchRoomParticipantRepository participantRepository = proxy(
                InTouchRoomParticipantRepository.class,
                (name, args) -> {
                    if (name.equals("findByRoomIdAndMobileUserId")) {
                        participantLookups[0]++;
                        return Optional.ofNullable(participant);
                    }
                    return unexpected(name);
                });
        InTouchRoomGroupLiveKeyRepository keyRepository = proxy(
                InTouchRoomGroupLiveKeyRepository.class,
                (name, args) -> {
                    if (name.equals("findMyNextAvailableKeys")) return List.of();
                    if (name.startsWith("countByRoomIdAndAssignedParticipantMobileUserId")) {
                        return 0L;
                    }
                    return unexpected(name);
                });
        SecurityUtils security = new SecurityUtils() {
            @Override
            public Integer getCurrentUserId() {
                return USER_ID;
            }
        };

        return new Fixture(
                new InTouchRoomMobileQueryService(
                        roomRepository,
                        keyRepository,
                        participantRepository,
                        new InTouchRoomLifecycleValidator(),
                        security),
                participantLookups);
    }

    private Object unexpected(String method) {
        throw new AssertionError("Unexpected call: " + method);
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> invocation.call(method.getName(), args));
    }

    private interface Invocation {
        Object call(String method, Object[] args);
    }

    private record Fixture(InTouchRoomMobileQueryService service, int[] lookups) {
        int participantLookups() {
            return lookups[0];
        }
    }
}
