package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.MobileNextKeyResponse;
import com.intouch.IntouchApps.security.SecurityUtils;
import com.intouch.IntouchApps.user.User;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InTouchRoomPooledKeyClaimServiceTest {

    private static final Long ROOM_ID = 10L;
    private static final Long GROUP_ID = 20L;
    private static final Long PARTICIPANT_ID = 30L;
    private static final Integer USER_ID = 40;

    @Test
    void ownBuildKeyIsReturnedBeforePoolAndNothingIsPublished() {
        Fixture fixture = fixture(
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                InTouchRoomStatus.STARTED,
                ParticipantStatus.ACTIVE,
                true);
        InTouchRoomGroupLiveKey own = key(fixture,
                fixture.participant(), LiveKeyAssignmentState.ASSIGNED,
                LiveKeyBuildStatus.NOT_STARTED, GROUP_ID);
        InTouchRoomGroupLiveKey pooled = key(fixture,
                null, LiveKeyAssignmentState.POOLED,
                LiveKeyBuildStatus.NOT_STARTED, GROUP_ID);
        fixture.ownKey()[0] = own;
        fixture.pooledKeys().add(pooled);

        MobileNextKeyResponse response = fixture.service().claimNextKey(ROOM_ID);

        assertThat(response.isNoMoreKeys()).isFalse();
        assertThat(response.getNextKey().getId()).isEqualTo(own.getId());
        assertThat(fixture.poolQueries()).isZero();
        assertThat(fixture.saved()).isEmpty();
        assertThat(fixture.published()).isEmpty();
        assertThat(pooled.getAssignmentState()).isEqualTo(LiveKeyAssignmentState.POOLED);
    }

    @Test
    void claimsExactlyOneBuildPoolKeyAndPreservesProvenanceAndWorkFields() {
        Fixture fixture = fixture(
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                InTouchRoomStatus.STARTED,
                ParticipantStatus.ACTIVE,
                true);
        InTouchRoomParticipant releasedFrom = InTouchRoomParticipant.builder().id(99L).build();
        InTouchRoomGroupLiveKey first = key(fixture,
                null, LiveKeyAssignmentState.POOLED,
                LiveKeyBuildStatus.NOT_STARTED, GROUP_ID);
        InTouchRoomGroupLiveKey second = key(fixture,
                null, LiveKeyAssignmentState.POOLED,
                LiveKeyBuildStatus.NOT_STARTED, GROUP_ID);
        Instant pooledAt = Instant.parse("2026-07-01T12:00:00Z");
        first.setReleasedFromParticipant(releasedFrom);
        first.setPooledAt(pooledAt);
        fixture.pooledKeys().addAll(List.of(first, second));

        String value = first.getKeyValue();
        Integer row = first.getCurrentRow();
        Integer column = first.getCurrentColumn();
        Integer targetRow = first.getTargetRow();
        Integer targetColumn = first.getTargetColumn();
        Integer order = first.getAssignedOrder();
        MobileNextKeyResponse response = fixture.service().claimNextKey(ROOM_ID);

        assertThat(response.getNextKey().getId()).isEqualTo(first.getId());
        assertThat(first.getAssignmentState()).isEqualTo(LiveKeyAssignmentState.ASSIGNED);
        assertThat(first.getAssignedParticipant()).isSameAs(fixture.participant());
        assertThat(first.getReleasedFromParticipant()).isSameAs(releasedFrom);
        assertThat(first.getPooledAt()).isEqualTo(pooledAt);
        assertThat(first.getStatus()).isEqualTo(LiveKeyBuildStatus.NOT_STARTED);
        assertThat(first.getKeyValue()).isEqualTo(value);
        assertThat(first.getCurrentRow()).isEqualTo(row);
        assertThat(first.getCurrentColumn()).isEqualTo(column);
        assertThat(first.getTargetRow()).isEqualTo(targetRow);
        assertThat(first.getTargetColumn()).isEqualTo(targetColumn);
        assertThat(first.getAssignedOrder()).isEqualTo(order);
        assertThat(second.getAssignmentState()).isEqualTo(LiveKeyAssignmentState.POOLED);
        assertThat(second.getAssignedParticipant()).isNull();
        assertThat(fixture.saved()).containsExactly(first);
        assertThat(fixture.published()).containsExactly(ROOM_ID);
    }

    @Test
    void removeModeUsesInProgressPoolStatus() {
        Fixture fixture = fixture(
                LiveRoomBuildMode.REMOVE_KEYS,
                InTouchRoomStatus.STARTED,
                ParticipantStatus.ACTIVE,
                true);
        InTouchRoomGroupLiveKey pooled = key(fixture,
                null, LiveKeyAssignmentState.POOLED,
                LiveKeyBuildStatus.IN_PROGRESS, GROUP_ID);
        fixture.pooledKeys().add(pooled);

        fixture.service().claimNextKey(ROOM_ID);

        assertThat(fixture.requestedStatus()).containsExactly("IN_PROGRESS");
        assertThat(pooled.getStatus()).isEqualTo(LiveKeyBuildStatus.IN_PROGRESS);
        assertThat(pooled.getAssignedParticipant()).isSameAs(fixture.participant());
    }

    @Test
    void poolSelectionIsRestrictedToResolvedGroup() {
        Fixture fixture = fixture(
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                InTouchRoomStatus.STARTED,
                ParticipantStatus.ACTIVE,
                true);
        InTouchRoomGroupLiveKey otherGroup = key(fixture,
                null, LiveKeyAssignmentState.POOLED,
                LiveKeyBuildStatus.NOT_STARTED, 21L);
        fixture.pooledKeys().add(otherGroup);

        MobileNextKeyResponse response = fixture.service().claimNextKey(ROOM_ID);

        assertThat(response.isNoMoreKeys()).isTrue();
        assertThat(otherGroup.getAssignmentState()).isEqualTo(LiveKeyAssignmentState.POOLED);
        assertThat(otherGroup.getAssignedParticipant()).isNull();
        assertThat(fixture.published()).isEmpty();
    }

    @Test
    void noOwnOrPoolKeyReturnsExistingNoMoreKeysResponseWithoutPublishing() {
        Fixture fixture = fixture(
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                InTouchRoomStatus.STARTED,
                ParticipantStatus.ACTIVE,
                true);

        MobileNextKeyResponse response = fixture.service().claimNextKey(ROOM_ID);

        assertThat(response.isNoMoreKeys()).isTrue();
        assertThat(response.getNextKey()).isNull();
        assertThat(fixture.saved()).isEmpty();
        assertThat(fixture.published()).isEmpty();
    }

    @Test
    void inactiveJoinedLeftAndInactiveInRoomParticipantsCannotClaim() {
        for (ParticipantStatus status : List.of(
                ParticipantStatus.INVITED,
                ParticipantStatus.JOINED,
                ParticipantStatus.LEFT)) {
            Fixture fixture = fixture(
                    LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                    InTouchRoomStatus.STARTED,
                    status,
                    true);
            assertInactiveRejected(fixture);
        }

        Fixture inactiveInRoom = fixture(
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                InTouchRoomStatus.STARTED,
                ParticipantStatus.ACTIVE,
                false);
        assertInactiveRejected(inactiveInRoom);
    }

    @Test
    void pausedCompletedCancelledAndReadyRoomsCannotClaim() {
        for (InTouchRoomStatus status : List.of(
                InTouchRoomStatus.PAUSED,
                InTouchRoomStatus.COMPLETED,
                InTouchRoomStatus.CANCELLED,
                InTouchRoomStatus.READY)) {
            Fixture fixture = fixture(
                    LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                    status,
                    ParticipantStatus.ACTIVE,
                    true);
            assertThatThrownBy(() -> fixture.service().claimNextKey(ROOM_ID))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(fixture.published()).isEmpty();
        }
    }

    @Test
    void completedRoomIsRejectedByCommandButPostWorkReturnsNoMoreKeys() {
        Fixture fixture = fixture(
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                InTouchRoomStatus.COMPLETED,
                ParticipantStatus.ACTIVE,
                true);

        assertThatThrownBy(() -> fixture.service().claimNextKey(ROOM_ID))
                .isInstanceOf(IllegalStateException.class);

        MobileNextKeyResponse response =
                fixture.service().nextKeyAfterSuccessfulWork(ROOM_ID);
        assertThat(response.isNoMoreKeys()).isTrue();
        assertThat(fixture.published()).isEmpty();
    }

    @Test
    void deletedRoomCannotClaimEvenIfItsStatusIsStarted() {
        Fixture fixture = fixture(
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                InTouchRoomStatus.STARTED,
                ParticipantStatus.ACTIVE,
                true);
        fixture.room().setDeleted(true);

        assertThatThrownBy(() -> fixture.service().claimNextKey(ROOM_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Deleted room cannot provide keys.");
        assertThat(fixture.published()).isEmpty();
    }

    @Test
    void repositoryUsesConfirmedTableColumnsAndSkipLocked() throws Exception {
        Method method = InTouchRoomGroupLiveKeyRepository.class.getMethod(
                "findNextPooledKeyForUpdate", Long.class, Long.class, String.class);
        Query query = method.getAnnotation(Query.class);

        assertThat(query.nativeQuery()).isTrue();
        assertThat(query.value())
                .contains("FROM intouch_room_group_live_key_tbl")
                .contains("room_id = :roomId")
                .contains("group_id = :groupId")
                .contains("assignment_state = 'POOLED'")
                .contains("assigned_participant_id IS NULL")
                .contains("status = :status")
                .contains("ORDER BY assigned_order ASC, id ASC")
                .contains("FOR UPDATE SKIP LOCKED")
                .contains("LIMIT 1");
    }

    @Test
    void claimantRowIsPessimisticallyLockedAndEndpointsSeparateReadFromClaim()
            throws Exception {
        Method lockMethod = InTouchRoomParticipantRepository.class.getMethod(
                "findByRoomIdAndMobileUserIdForUpdate", Long.class, Integer.class);
        assertThat(lockMethod.getAnnotation(Lock.class).value())
                .isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(lockMethod.getAnnotation(Query.class).value())
                .contains("p.room.id = :roomId")
                .contains("p.mobileUser.id = :userId");

        Method get = InTouchRoomMobileController.class.getMethod("getNextKey", Long.class);
        assertThat(get.getAnnotation(GetMapping.class).value())
                .containsExactly("/{roomId}/next-key");
        Method post = InTouchRoomMobileController.class.getMethod("claimNextKey", Long.class);
        assertThat(post.getAnnotation(PostMapping.class).value())
                .containsExactly("/{roomId}/next-key/claim");
    }

    @Test
    void sequentialClaimsCannotReceiveTheSamePooledKey() {
        Fixture first = fixture(
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                InTouchRoomStatus.STARTED,
                ParticipantStatus.ACTIVE,
                true);
        InTouchRoomGroupLiveKey pooled = key(first,
                null, LiveKeyAssignmentState.POOLED,
                LiveKeyBuildStatus.NOT_STARTED, GROUP_ID);
        first.pooledKeys().add(pooled);

        MobileNextKeyResponse firstResponse = first.service().claimNextKey(ROOM_ID);
        first.ownKey()[0] = pooled;
        MobileNextKeyResponse secondResponse = first.service().claimNextKey(ROOM_ID);

        assertThat(firstResponse.getNextKey().getId()).isEqualTo(pooled.getId());
        assertThat(secondResponse.getNextKey().getId()).isEqualTo(pooled.getId());
        assertThat(first.saved()).containsExactly(pooled);
        assertThat(first.poolQueries()).isEqualTo(1);
    }

    private void assertInactiveRejected(Fixture fixture) {
        assertThatThrownBy(() -> fixture.service().claimNextKey(ROOM_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Participant is not active in this room.");
        assertThat(fixture.published()).isEmpty();
    }

    private Fixture fixture(
            LiveRoomBuildMode buildMode,
            InTouchRoomStatus roomStatus,
            ParticipantStatus participantStatus,
            boolean activeInRoom
    ) {
        InTouchRoom room = InTouchRoom.builder()
                .id(ROOM_ID)
                .title("Room")
                .buildMode(buildMode)
                .status(roomStatus)
                .deleted(false)
                .build();
        InTouchRoomParticipant participant = InTouchRoomParticipant.builder()
                .id(PARTICIPANT_ID)
                .room(room)
                .mobileUser(User.builder().id(USER_ID).build())
                .status(participantStatus)
                .activeInRoom(activeInRoom)
                .build();
        InTouchRoomGroup group = InTouchRoomGroup.builder()
                .id(GROUP_ID)
                .room(room)
                .name("Group")
                .build();
        InTouchRoomGroupParticipant assignment = InTouchRoomGroupParticipant.builder()
                .room(room)
                .group(group)
                .participant(participant)
                .build();
        InTouchRoomGroupLiveKey[] ownKey = {null};
        List<InTouchRoomGroupLiveKey> pooledKeys = new ArrayList<>();
        List<InTouchRoomGroupLiveKey> saved = new ArrayList<>();
        List<Long> published = new ArrayList<>();
        List<String> requestedStatus = new ArrayList<>();
        int[] poolQueries = {0};

        InTouchRoomRepository roomRepository = proxy(
                InTouchRoomRepository.class,
                (name, args) -> name.equals("findById")
                        ? Optional.of(room)
                        : unexpected(name));
        InTouchRoomParticipantRepository participantRepository = proxy(
                InTouchRoomParticipantRepository.class,
                (name, args) -> name.equals("findByRoomIdAndMobileUserIdForUpdate")
                        ? Optional.of(participant)
                        : unexpected(name));
        InTouchRoomGroupParticipantRepository assignmentRepository = proxy(
                InTouchRoomGroupParticipantRepository.class,
                (name, args) -> name.equals("findByRoomIdAndParticipantId")
                        ? List.of(assignment)
                        : unexpected(name));
        InTouchRoomGroupLiveKeyRepository keyRepository = proxy(
                InTouchRoomGroupLiveKeyRepository.class,
                (name, args) -> {
                    if (name.startsWith("findFirstByRoom_IdAndGroup_IdAndAssignedParticipant")) {
                        return Optional.ofNullable(ownKey[0]);
                    }
                    if (name.equals("findNextPooledKeyForUpdate")) {
                        poolQueries[0]++;
                        assertThat(args[0]).isEqualTo(ROOM_ID);
                        assertThat(args[1]).isEqualTo(GROUP_ID);
                        requestedStatus.add((String) args[2]);
                        return pooledKeys.stream()
                                .filter(key -> key.getRoom().getId().equals(ROOM_ID))
                                .filter(key -> key.getGroup().getId().equals(GROUP_ID))
                                .filter(key -> key.getAssignmentState() ==
                                        LiveKeyAssignmentState.POOLED)
                                .filter(key -> key.getAssignedParticipant() == null)
                                .filter(key -> key.getStatus().name().equals(args[2]))
                                .findFirst();
                    }
                    if (name.equals("saveAndFlush")) {
                        saved.add((InTouchRoomGroupLiveKey) args[0]);
                        return args[0];
                    }
                    return unexpected(name);
                });
        InTouchRoomMobileQueryService queryService =
                new InTouchRoomMobileQueryService(null, null, null, null, null) {
                    @Override
                    MobileNextKeyResponse buildNextKeyResponse(
                            InTouchRoom ignoredRoom,
                            Integer ignoredUserId
                    ) {
                        InTouchRoomGroupLiveKey next = ownKey[0];
                        if (next == null) {
                            next = pooledKeys.stream()
                                    .filter(key -> key.getAssignedParticipant() == participant)
                                    .filter(key -> key.getAssignmentState() ==
                                            LiveKeyAssignmentState.ASSIGNED)
                                    .findFirst()
                                    .orElse(null);
                        }
                        if (next == null) {
                            return MobileNextKeyResponse.builder()
                                    .roomId(ROOM_ID)
                                    .noMoreKeys(true)
                                    .nextKey(null)
                                    .build();
                        }
                        return MobileNextKeyResponse.builder()
                                .roomId(ROOM_ID)
                                .groupId(next.getGroup().getId())
                                .nextKey(com.intouch.IntouchApps.liveroom.dto.response
                                        .MobileLiveKeyResponse.builder()
                                        .id(next.getId())
                                        .status(next.getStatus())
                                        .build())
                                .noMoreKeys(false)
                                .build();
                    }
                };
        InTouchRoomProgressPublisher publisher = new InTouchRoomProgressPublisher(null, null) {
            @Override
            public void publishRoomProgress(Long roomId) {
                published.add(roomId);
            }
        };
        SecurityUtils security = new SecurityUtils() {
            @Override
            public Integer getCurrentUserId() {
                return USER_ID;
            }
        };
        InTouchRoomPooledKeyClaimService service = new InTouchRoomPooledKeyClaimService(
                roomRepository,
                participantRepository,
                assignmentRepository,
                keyRepository,
                new InTouchRoomLifecycleValidator(),
                queryService,
                publisher,
                security);

        return new Fixture(
                service,
                room,
                participant,
                ownKey,
                pooledKeys,
                saved,
                published,
                requestedStatus,
                poolQueries);
    }

    private InTouchRoomGroupLiveKey key(
            Fixture fixture,
            InTouchRoomParticipant assignedParticipant,
            LiveKeyAssignmentState assignmentState,
            LiveKeyBuildStatus status,
            Long groupId
    ) {
        InTouchRoomGroup group = InTouchRoomGroup.builder()
                .id(groupId)
                .room(fixture.room())
                .name("Group " + groupId)
                .build();
        return InTouchRoomGroupLiveKey.builder()
                .id((long) (fixture.pooledKeys().size() + 1))
                .room(fixture.room())
                .group(group)
                .assignedParticipant(assignedParticipant)
                .assignmentState(assignmentState)
                .keyValue("A")
                .keyType(LiveKeyType.CUSTOM)
                .assignedOrder(7)
                .currentRow(2)
                .currentColumn(3)
                .targetRow(4)
                .targetColumn(5)
                .status(status)
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
        Object call(String method, Object[] args);
    }

    private record Fixture(
            InTouchRoomPooledKeyClaimService service,
            InTouchRoom room,
            InTouchRoomParticipant participant,
            InTouchRoomGroupLiveKey[] ownKey,
            List<InTouchRoomGroupLiveKey> pooledKeys,
            List<InTouchRoomGroupLiveKey> saved,
            List<Long> published,
            List<String> requestedStatus,
            int[] poolQueriesHolder
    ) {
        int poolQueries() {
            return poolQueriesHolder[0];
        }

    }
}
