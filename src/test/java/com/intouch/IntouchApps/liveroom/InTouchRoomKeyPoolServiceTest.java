package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.ReleaseUnfinishedKeysResponse;
import com.intouch.IntouchApps.security.SecurityUtils;
import com.intouch.IntouchApps.user.User;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.PatchMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InTouchRoomKeyPoolServiceTest {

    private static final Integer OWNER_ID = 42;
    private static final Long ROOM_ID = 10L;
    private static final Long PARTICIPANT_ID = 20L;
    private static final Long GROUP_ID = 30L;

    @Test
    void buildModeReleasesOnlyAssignedNotStartedKeysInParticipantsGroup() {
        Fixture fixture = fixture(
                InTouchRoomStatus.STARTED,
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                ParticipantStatus.LEFT,
                OWNER_ID,
                true,
                true);
        InTouchRoomGroupLiveKey releasable = key(fixture,
                GROUP_ID, fixture.participant(), LiveKeyAssignmentState.ASSIGNED,
                LiveKeyBuildStatus.NOT_STARTED);
        InTouchRoomGroupLiveKey placed = key(fixture,
                GROUP_ID, fixture.participant(), LiveKeyAssignmentState.ASSIGNED,
                LiveKeyBuildStatus.PLACED);
        InTouchRoomGroupLiveKey secondReleasable = key(fixture,
                GROUP_ID, fixture.participant(), LiveKeyAssignmentState.ASSIGNED,
                LiveKeyBuildStatus.NOT_STARTED);
        InTouchRoomGroupLiveKey otherGroup = key(fixture,
                31L, fixture.participant(), LiveKeyAssignmentState.ASSIGNED,
                LiveKeyBuildStatus.NOT_STARTED);
        fixture.keys().addAll(List.of(releasable, placed, secondReleasable, otherGroup));

        String keyValue = releasable.getKeyValue();
        Integer row = releasable.getCurrentRow();
        Integer column = releasable.getCurrentColumn();
        Integer order = releasable.getAssignedOrder();
        Instant placedAt = placed.getPlacedAt();
        ReleaseUnfinishedKeysResponse response = fixture.service()
                .releaseUnfinishedKeys(ROOM_ID, PARTICIPANT_ID);

        assertThat(response.releasedKeyCount()).isEqualTo(2);
        assertReleased(releasable, fixture.participant());
        assertReleased(secondReleasable, fixture.participant());
        assertThat(secondReleasable.getPooledAt()).isEqualTo(releasable.getPooledAt());
        assertThat(releasable.getKeyValue()).isEqualTo(keyValue);
        assertThat(releasable.getCurrentRow()).isEqualTo(row);
        assertThat(releasable.getCurrentColumn()).isEqualTo(column);
        assertThat(releasable.getAssignedOrder()).isEqualTo(order);
        assertThat(releasable.getStatus()).isEqualTo(LiveKeyBuildStatus.NOT_STARTED);
        assertUntouched(placed, fixture.participant(), LiveKeyBuildStatus.PLACED);
        assertThat(placed.getPlacedAt()).isEqualTo(placedAt);
        assertUntouched(otherGroup, fixture.participant(), LiveKeyBuildStatus.NOT_STARTED);
        assertThat(fixture.published()).containsExactly(ROOM_ID);
    }

    @Test
    void removeModeReleasesOnlyAssignedInProgressKeysAndPreservesRemovedKeys() {
        Fixture fixture = fixture(
                InTouchRoomStatus.PAUSED,
                LiveRoomBuildMode.REMOVE_KEYS,
                ParticipantStatus.LEFT,
                OWNER_ID,
                true,
                true);
        InTouchRoomGroupLiveKey releasable = key(fixture,
                GROUP_ID, fixture.participant(), LiveKeyAssignmentState.ASSIGNED,
                LiveKeyBuildStatus.IN_PROGRESS);
        InTouchRoomGroupLiveKey removed = key(fixture,
                GROUP_ID, fixture.participant(), LiveKeyAssignmentState.ASSIGNED,
                LiveKeyBuildStatus.REMOVED);
        fixture.keys().addAll(List.of(releasable, removed));

        ReleaseUnfinishedKeysResponse response = fixture.service()
                .releaseUnfinishedKeys(ROOM_ID, PARTICIPANT_ID);

        assertThat(response.releasedKeyCount()).isEqualTo(1);
        assertReleased(releasable, fixture.participant());
        assertThat(releasable.getStatus()).isEqualTo(LiveKeyBuildStatus.IN_PROGRESS);
        assertUntouched(removed, fixture.participant(), LiveKeyBuildStatus.REMOVED);
        assertThat(removed.getRemovedAt()).isNotNull();
    }

    @Test
    void participantMustBeLeft() {
        Fixture fixture = fixture(
                InTouchRoomStatus.STARTED,
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                ParticipantStatus.ACTIVE,
                OWNER_ID,
                true,
                true);

        assertRejected(fixture, "Only a participant who left may have unfinished keys released.");
    }

    @Test
    void roomMustBeStartedOrPaused() {
        Fixture fixture = fixture(
                InTouchRoomStatus.READY,
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                ParticipantStatus.LEFT,
                OWNER_ID,
                true,
                true);

        assertRejected(
                fixture,
                "Unfinished keys can only be released from a started or paused room.");
    }

    @Test
    void participantMustBelongToRoom() {
        Fixture fixture = fixture(
                InTouchRoomStatus.STARTED,
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                ParticipantStatus.LEFT,
                OWNER_ID,
                false,
                true);

        assertThatThrownBy(() -> fixture.service()
                .releaseUnfinishedKeys(ROOM_ID, PARTICIPANT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Participant not found.");
        assertThat(fixture.published()).isEmpty();
    }

    @Test
    void participantMustHaveGroupAssignment() {
        Fixture fixture = fixture(
                InTouchRoomStatus.STARTED,
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                ParticipantStatus.LEFT,
                OWNER_ID,
                true,
                false);

        assertRejected(
                fixture,
                "Participant must be assigned to a group before keys can be released.");
    }

    @Test
    void repeatedReleaseDoesNotAlterAlreadyPooledKeys() {
        Fixture fixture = fixture(
                InTouchRoomStatus.STARTED,
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                ParticipantStatus.LEFT,
                OWNER_ID,
                true,
                true);
        InTouchRoomGroupLiveKey key = key(fixture,
                GROUP_ID, fixture.participant(), LiveKeyAssignmentState.ASSIGNED,
                LiveKeyBuildStatus.NOT_STARTED);
        fixture.keys().add(key);

        fixture.service().releaseUnfinishedKeys(ROOM_ID, PARTICIPANT_ID);
        Instant firstPooledAt = key.getPooledAt();

        assertThatThrownBy(() -> fixture.service()
                .releaseUnfinishedKeys(ROOM_ID, PARTICIPANT_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This participant has no unfinished keys available to release.");
        assertThat(key.getPooledAt()).isEqualTo(firstPooledAt);
        assertReleased(key, fixture.participant());
        assertThat(fixture.published()).containsExactly(ROOM_ID);
    }

    @Test
    void releaseDoesNotAlterKeySubsequentlyClaimedByAnotherParticipant() {
        Fixture fixture = fixture(
                InTouchRoomStatus.STARTED,
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                ParticipantStatus.LEFT,
                OWNER_ID,
                true,
                true);
        InTouchRoomParticipant claimant = InTouchRoomParticipant.builder().id(99L).build();
        InTouchRoomGroupLiveKey claimedKey = key(
                fixture,
                GROUP_ID,
                claimant,
                LiveKeyAssignmentState.ASSIGNED,
                LiveKeyBuildStatus.NOT_STARTED);
        claimedKey.setReleasedFromParticipant(fixture.participant());
        Instant originalPooledAt = Instant.parse("2026-07-01T12:00:00Z");
        claimedKey.setPooledAt(originalPooledAt);
        fixture.keys().add(claimedKey);

        assertThatThrownBy(() -> fixture.service()
                .releaseUnfinishedKeys(ROOM_ID, PARTICIPANT_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This participant has no unfinished keys available to release.");
        assertThat(claimedKey.getAssignedParticipant()).isSameAs(claimant);
        assertThat(claimedKey.getAssignmentState()).isEqualTo(LiveKeyAssignmentState.ASSIGNED);
        assertThat(claimedKey.getPooledAt()).isEqualTo(originalPooledAt);
        assertThat(fixture.published()).isEmpty();
    }

    @Test
    void repositoriesAndEndpointDeclareTheRequiredLockingAndRoute() throws Exception {
        Method participantLockMethod = InTouchRoomParticipantRepository.class.getMethod(
                "findByIdAndRoomIdForUpdate", Long.class, Long.class);
        Lock participantLock = participantLockMethod.getAnnotation(Lock.class);
        Query participantQuery = participantLockMethod.getAnnotation(Query.class);
        assertThat(participantLock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(participantQuery.value())
                .contains("p.id = :participantId")
                .contains("p.room.id = :roomId");

        Method keyLockMethod = InTouchRoomGroupLiveKeyRepository.class.getMethod(
                "findReleasableKeysForUpdate",
                Long.class,
                Long.class,
                Long.class,
                LiveKeyAssignmentState.class,
                LiveKeyBuildStatus.class);
        assertThat(keyLockMethod.getAnnotation(Lock.class).value())
                .isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(keyLockMethod.getAnnotation(Query.class).value())
                .contains("k.room.id = :roomId")
                .contains("k.group.id = :groupId")
                .contains("k.assignedParticipant.id = :participantId")
                .contains("k.assignmentState = :assignmentState")
                .contains("k.status = :status");

        Method endpoint = InTouchRoomOwnerController.class.getMethod(
                "releaseUnfinishedKeys", Long.class, Long.class);
        assertThat(endpoint.getAnnotation(PatchMapping.class).value())
                .containsExactly(
                        "/{roomId}/participants/{participantId}/release-unfinished-keys");
    }

    @Test
    void reactivationAfterReleaseDoesNotReclaimPooledKey() {
        Fixture fixture = fixture(
                InTouchRoomStatus.STARTED,
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                ParticipantStatus.LEFT,
                OWNER_ID,
                true,
                true);
        InTouchRoomGroupLiveKey key = key(fixture,
                GROUP_ID, fixture.participant(), LiveKeyAssignmentState.ASSIGNED,
                LiveKeyBuildStatus.NOT_STARTED);
        fixture.keys().add(key);

        fixture.service().releaseUnfinishedKeys(ROOM_ID, PARTICIPANT_ID);
        fixture.reactivationService().reactivateParticipant(ROOM_ID, PARTICIPANT_ID);

        assertThat(fixture.participant().getStatus()).isEqualTo(ParticipantStatus.ACTIVE);
        assertReleased(key, fixture.participant());
    }

    @Test
    void unauthorizedUserIsRejectedWithoutPublishing() {
        Fixture fixture = fixture(
                InTouchRoomStatus.STARTED,
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                ParticipantStatus.LEFT,
                99,
                true,
                true);

        assertRejected(fixture, "You are not the owner of this room.");
    }

    @Test
    void noReleasableKeysUsesBusinessConflictAndDoesNotPublish() {
        Fixture fixture = fixture(
                InTouchRoomStatus.STARTED,
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                ParticipantStatus.LEFT,
                OWNER_ID,
                true,
                true);

        assertRejected(
                fixture,
                "This participant has no unfinished keys available to release.");
    }

    private void assertReleased(
            InTouchRoomGroupLiveKey key,
            InTouchRoomParticipant originalParticipant
    ) {
        assertThat(key.getAssignmentState()).isEqualTo(LiveKeyAssignmentState.POOLED);
        assertThat(key.getAssignedParticipant()).isNull();
        assertThat(key.getReleasedFromParticipant()).isSameAs(originalParticipant);
        assertThat(key.getPooledAt()).isNotNull();
    }

    private void assertUntouched(
            InTouchRoomGroupLiveKey key,
            InTouchRoomParticipant participant,
            LiveKeyBuildStatus status
    ) {
        assertThat(key.getAssignmentState()).isEqualTo(LiveKeyAssignmentState.ASSIGNED);
        assertThat(key.getAssignedParticipant()).isSameAs(participant);
        assertThat(key.getReleasedFromParticipant()).isNull();
        assertThat(key.getPooledAt()).isNull();
        assertThat(key.getStatus()).isEqualTo(status);
    }

    private void assertRejected(Fixture fixture, String message) {
        assertThatThrownBy(() -> fixture.service()
                .releaseUnfinishedKeys(ROOM_ID, PARTICIPANT_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(message);
        assertThat(fixture.published()).isEmpty();
    }

    private Fixture fixture(
            InTouchRoomStatus roomStatus,
            LiveRoomBuildMode buildMode,
            ParticipantStatus participantStatus,
            Integer currentUserId,
            boolean participantFound,
            boolean groupAssignmentExists
    ) {
        InTouchRoom room = InTouchRoom.builder()
                .id(ROOM_ID)
                .owner(User.builder().id(OWNER_ID).build())
                .status(roomStatus)
                .buildMode(buildMode)
                .deleted(false)
                .build();
        InTouchRoomParticipant participant = InTouchRoomParticipant.builder()
                .id(PARTICIPANT_ID)
                .room(room)
                .status(participantStatus)
                .activeInRoom(false)
                .build();
        InTouchRoomGroup group = InTouchRoomGroup.builder().id(GROUP_ID).room(room).build();
        InTouchRoomGroupParticipant assignment = InTouchRoomGroupParticipant.builder()
                .room(room)
                .group(group)
                .participant(participant)
                .build();
        List<InTouchRoomGroupLiveKey> keys = new ArrayList<>();
        List<Long> published = new ArrayList<>();

        InTouchRoomRepository roomRepository = proxy(
                InTouchRoomRepository.class,
                (name, args) -> name.equals("findById")
                        ? Optional.of(room)
                        : unexpected(name));
        InTouchRoomParticipantRepository participantRepository = proxy(
                InTouchRoomParticipantRepository.class,
                (name, args) -> {
                    if (name.equals("findByIdAndRoomIdForUpdate")) {
                        return participantFound ? Optional.of(participant) : Optional.empty();
                    }
                    if (name.equals("save")) return args[0];
                    return unexpected(name);
                });
        InTouchRoomGroupParticipantRepository assignmentRepository = proxy(
                InTouchRoomGroupParticipantRepository.class,
                (name, args) -> {
                    if (name.equals("findByRoomIdAndParticipantId")) {
                        return groupAssignmentExists ? List.of(assignment) : List.of();
                    }
                    if (name.equals("existsByRoomIdAndParticipantId")) {
                        return groupAssignmentExists;
                    }
                    return unexpected(name);
                });
        InTouchRoomGroupLiveKeyRepository keyRepository = proxy(
                InTouchRoomGroupLiveKeyRepository.class,
                (name, args) -> {
                    if (name.equals("findReleasableKeysForUpdate")) {
                        assertThat(args).containsExactly(
                                ROOM_ID,
                                GROUP_ID,
                                PARTICIPANT_ID,
                                LiveKeyAssignmentState.ASSIGNED,
                                buildMode == LiveRoomBuildMode.REMOVE_KEYS
                                        ? LiveKeyBuildStatus.IN_PROGRESS
                                        : LiveKeyBuildStatus.NOT_STARTED);
                        return keys.stream()
                                .filter(key -> key.getRoom().getId().equals(ROOM_ID))
                                .filter(key -> key.getGroup().getId().equals(GROUP_ID))
                                .filter(key -> key.getAssignedParticipant() == participant)
                                .filter(key -> key.getAssignmentState() ==
                                        LiveKeyAssignmentState.ASSIGNED)
                                .filter(key -> key.getStatus() == args[4])
                                .toList();
                    }
                    if (name.equals("saveAll")) return args[0];
                    return unexpected(name);
                });
        SecurityUtils security = new SecurityUtils() {
            @Override
            public Integer getCurrentUserId() {
                return currentUserId;
            }
        };
        InTouchRoomAccessValidator accessValidator =
                new InTouchRoomAccessValidator(security);
        InTouchRoomProgressPublisher publisher = new InTouchRoomProgressPublisher(null, null) {
            @Override
            public void publishRoomProgress(Long roomId) {
                published.add(roomId);
            }
        };
        InTouchRoomKeyPoolService service = new InTouchRoomKeyPoolService(
                roomRepository,
                participantRepository,
                assignmentRepository,
                keyRepository,
                accessValidator,
                publisher);
        InTouchRoomOwnerCommandService reactivationService =
                new InTouchRoomOwnerCommandService(
                        roomRepository, null, participantRepository, assignmentRepository,
                        null, null, null, accessValidator, null, security, null, null, null,
                        null, publisher, null, null, null);

        return new Fixture(
                service,
                reactivationService,
                room,
                participant,
                keys,
                published);
    }

    private InTouchRoomGroupLiveKey key(
            Fixture fixture,
            Long groupId,
            InTouchRoomParticipant participant,
            LiveKeyAssignmentState assignmentState,
            LiveKeyBuildStatus status
    ) {
        InTouchRoomGroup group = InTouchRoomGroup.builder()
                .id(groupId)
                .room(fixture.room())
                .build();
        return InTouchRoomGroupLiveKey.builder()
                .room(fixture.room())
                .group(group)
                .assignedParticipant(participant)
                .assignmentState(assignmentState)
                .keyValue("A")
                .keyType(LiveKeyType.CUSTOM)
                .assignedOrder(7)
                .currentRow(2)
                .currentColumn(3)
                .targetRow(2)
                .targetColumn(3)
                .status(status)
                .placedAt(status == LiveKeyBuildStatus.PLACED ? Instant.now() : null)
                .removedAt(status == LiveKeyBuildStatus.REMOVED ? Instant.now() : null)
                .build();
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
        Object call(String name, Object[] args);
    }

    private record Fixture(
            InTouchRoomKeyPoolService service,
            InTouchRoomOwnerCommandService reactivationService,
            InTouchRoom room,
            InTouchRoomParticipant participant,
            List<InTouchRoomGroupLiveKey> keys,
            List<Long> published
    ) {
    }
}
