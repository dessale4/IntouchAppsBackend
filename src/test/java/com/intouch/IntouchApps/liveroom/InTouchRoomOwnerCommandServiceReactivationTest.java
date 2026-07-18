package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.security.SecurityUtils;
import com.intouch.IntouchApps.user.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InTouchRoomOwnerCommandServiceReactivationTest {

    private static final Integer OWNER_ID = 42;
    private static final Long ROOM_ID = 10L;
    private static final Long PARTICIPANT_ID = 20L;

    @Test
    void ownerReactivatesLeftParticipantInReadyAndPreservesHistory() {
        Fixture fixture = fixture(InTouchRoomStatus.READY, ParticipantStatus.LEFT, OWNER_ID, true);
        Instant claimedAt = fixture.participant().getClaimedAt();
        User user = fixture.participant().getMobileUser();

        fixture.service().reactivateParticipant(ROOM_ID, PARTICIPANT_ID);

        assertThat(fixture.participant().getStatus()).isEqualTo(ParticipantStatus.JOINED);
        assertThat(fixture.participant().getActiveInRoom()).isFalse();
        assertThat(fixture.participant().getMobileUser()).isSameAs(user);
        assertThat(fixture.participant().getClaimedAt()).isEqualTo(claimedAt);
        assertThat(fixture.participant().getParticipantCode()).isEqualTo("1234");
        assertThat(fixture.participant().getDisplayName()).isEqualTo("Participant");
        assertThat(fixture.repositoryCalls()).containsExactly("findByIdAndRoomIdForUpdate", "save");
        assertThat(fixture.publishedRoomIds()).containsExactly(ROOM_ID);
    }

    @Test
    void ownerReactivatesLeftParticipantInStartedAndPreservesExistingActivatedAt() {
        Fixture fixture = fixture(InTouchRoomStatus.STARTED, ParticipantStatus.LEFT, OWNER_ID, true);
        Instant activatedAt = fixture.participant().getActivatedAt();

        fixture.service().reactivateParticipant(ROOM_ID, PARTICIPANT_ID);

        assertThat(fixture.participant().getStatus()).isEqualTo(ParticipantStatus.ACTIVE);
        assertThat(fixture.participant().getActiveInRoom()).isTrue();
        assertThat(fixture.participant().getActivatedAt()).isEqualTo(activatedAt);
    }

    @Test
    void ownerReactivatesLeftParticipantInPausedAndInitializesMissingActivatedAt() {
        Fixture fixture = fixture(InTouchRoomStatus.PAUSED, ParticipantStatus.LEFT, OWNER_ID, true);
        fixture.participant().setActivatedAt(null);

        fixture.service().reactivateParticipant(ROOM_ID, PARTICIPANT_ID);

        assertThat(fixture.participant().getStatus()).isEqualTo(ParticipantStatus.ACTIVE);
        assertThat(fixture.participant().getActiveInRoom()).isTrue();
        assertThat(fixture.participant().getActivatedAt()).isNotNull();
    }

    @Test
    void startedRoomWithoutGroupAssignmentIsRejected() {
        Fixture fixture = fixture(
                InTouchRoomStatus.STARTED,
                ParticipantStatus.LEFT,
                OWNER_ID,
                true,
                false
        );

        assertThatThrownBy(() -> fixture.service().reactivateParticipant(ROOM_ID, PARTICIPANT_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Participant must be assigned to a group before reactivation.");
        assertThat(fixture.participant().getStatus()).isEqualTo(ParticipantStatus.LEFT);
    }

    @Test
    void pausedRoomWithoutGroupAssignmentIsRejected() {
        Fixture fixture = fixture(
                InTouchRoomStatus.PAUSED,
                ParticipantStatus.LEFT,
                OWNER_ID,
                true,
                false
        );

        assertThatThrownBy(() -> fixture.service().reactivateParticipant(ROOM_ID, PARTICIPANT_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Participant must be assigned to a group before reactivation.");
        assertThat(fixture.participant().getStatus()).isEqualTo(ParticipantStatus.LEFT);
    }

    @Test
    void readyRoomWithoutGroupAssignmentMayBeReactivated() {
        Fixture fixture = fixture(
                InTouchRoomStatus.READY,
                ParticipantStatus.LEFT,
                OWNER_ID,
                true,
                false
        );

        fixture.service().reactivateParticipant(ROOM_ID, PARTICIPANT_ID);

        assertThat(fixture.participant().getStatus()).isEqualTo(ParticipantStatus.JOINED);
        assertThat(fixture.participant().getActiveInRoom()).isFalse();
    }

    @Test
    void nonOwnerIsRejected() {
        Fixture fixture = fixture(InTouchRoomStatus.READY, ParticipantStatus.LEFT, 99, true);

        assertThatThrownBy(() -> fixture.service().reactivateParticipant(ROOM_ID, PARTICIPANT_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You are not the owner of this room.");
        assertThat(fixture.repositoryCalls()).isEmpty();
    }

    @Test
    void participantFromAnotherRoomIsRejected() {
        Fixture fixture = fixture(InTouchRoomStatus.READY, ParticipantStatus.LEFT, OWNER_ID, false);

        assertThatThrownBy(() -> fixture.service().reactivateParticipant(ROOM_ID, PARTICIPANT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Participant not found.");
    }

    @Test
    void nonLeftParticipantIsRejected() {
        Fixture fixture = fixture(InTouchRoomStatus.READY, ParticipantStatus.JOINED, OWNER_ID, true);

        assertThatThrownBy(() -> fixture.service().reactivateParticipant(ROOM_ID, PARTICIPANT_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Only a participant who left may be reactivated.");
    }

    @Test
    void terminalRoomIsRejected() {
        Fixture fixture = fixture(InTouchRoomStatus.COMPLETED, ParticipantStatus.LEFT, OWNER_ID, true);

        assertThatThrownBy(() -> fixture.service().reactivateParticipant(ROOM_ID, PARTICIPANT_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Participant cannot be reactivated in this room.");
        assertThat(fixture.repositoryCalls()).isEmpty();
    }

    private Fixture fixture(
            InTouchRoomStatus roomStatus,
            ParticipantStatus participantStatus,
            Integer currentUserId,
            boolean participantFound
    ) {
        return fixture(roomStatus, participantStatus, currentUserId, participantFound, true);
    }

    private Fixture fixture(
            InTouchRoomStatus roomStatus,
            ParticipantStatus participantStatus,
            Integer currentUserId,
            boolean participantFound,
            boolean groupAssignmentExists
    ) {
        User owner = User.builder().id(OWNER_ID).build();
        InTouchRoom room = InTouchRoom.builder()
                .id(ROOM_ID).owner(owner).status(roomStatus).deleted(false).build();
        InTouchRoomParticipant participant = InTouchRoomParticipant.builder()
                .id(PARTICIPANT_ID).room(room).status(participantStatus).activeInRoom(false)
                .mobileUser(User.builder().id(7).build()).participantCode("1234")
                .displayName("Participant").claimedAt(Instant.parse("2026-07-01T10:00:00Z"))
                .activatedAt(Instant.parse("2026-07-01T11:00:00Z")).build();

        InTouchRoomRepository roomRepository = proxy(InTouchRoomRepository.class,
                (name, args) -> name.equals("findById") ? Optional.of(room) : unexpected(name));
        List<String> calls = new ArrayList<>();
        InTouchRoomParticipantRepository participantRepository = proxy(
                InTouchRoomParticipantRepository.class,
                (name, args) -> {
                    calls.add(name);
                    if (name.equals("findByIdAndRoomIdForUpdate")) {
                        return participantFound ? Optional.of(participant) : Optional.empty();
                    }
                    if (name.equals("save")) return args[0];
                    return unexpected(name);
                });
        InTouchRoomGroupParticipantRepository groupParticipantRepository = proxy(
                InTouchRoomGroupParticipantRepository.class,
                (name, args) -> {
                    if (name.equals("existsByRoomIdAndParticipantId")) {
                        assertThat(args).containsExactly(ROOM_ID, PARTICIPANT_ID);
                        return groupAssignmentExists;
                    }
                    return unexpected(name);
                });
        SecurityUtils security = new SecurityUtils() {
            @Override public Integer getCurrentUserId() { return currentUserId; }
        };
        List<Long> published = new ArrayList<>();
        InTouchRoomProgressPublisher publisher = new InTouchRoomProgressPublisher(null, null) {
            @Override public void publishRoomProgress(Long roomId) { published.add(roomId); }
        };
        InTouchRoomOwnerCommandService service = new InTouchRoomOwnerCommandService(
                roomRepository, null, participantRepository, groupParticipantRepository,
                null, null, null,
                new InTouchRoomAccessValidator(security), null, security, null, null, null,
                null, publisher, null, null, null);
        return new Fixture(service, participant, calls, published);
    }

    private Object unexpected(String name) { throw new AssertionError("Unexpected call: " + name); }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> invocation.call(method.getName(), args));
    }

    private interface Invocation { Object call(String name, Object[] args); }
    private record Fixture(InTouchRoomOwnerCommandService service,
                           InTouchRoomParticipant participant,
                           List<String> repositoryCalls,
                           List<Long> publishedRoomIds) {}
}
