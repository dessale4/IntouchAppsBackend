package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.LiveRoomParticipantAccessResponse;
import com.intouch.IntouchApps.liveroom.dto.response.LiveRoomParticipantAccessRowResponse;
import com.intouch.IntouchApps.security.SecurityUtils;
import com.intouch.IntouchApps.user.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InTouchRoomParticipantAccessCountsTest {

    private static final Integer OWNER_ID = 42;
    private static final Long ROOM_ID = 10L;
    private static final Long GROUP_ID = 20L;

    @Test
    void buildModeCountsOnlyAssignedNotStartedAndPreservesReleaseHistory() {
        Fixture fixture = fixture(LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY);
        InTouchRoomParticipant participant = fixture.participant();
        InTouchRoomParticipant claimant = InTouchRoomParticipant.builder().id(99L).build();

        fixture.keys().add(key(
                fixture.room(), fixture.group(), participant, null,
                LiveKeyAssignmentState.ASSIGNED, LiveKeyBuildStatus.NOT_STARTED));
        fixture.keys().add(key(
                fixture.room(), fixture.group(), participant, null,
                LiveKeyAssignmentState.ASSIGNED, LiveKeyBuildStatus.PLACED));
        fixture.keys().add(key(
                fixture.room(), fixture.group(), null, participant,
                LiveKeyAssignmentState.POOLED, LiveKeyBuildStatus.NOT_STARTED));
        fixture.keys().add(key(
                fixture.room(), fixture.group(), claimant, participant,
                LiveKeyAssignmentState.ASSIGNED, LiveKeyBuildStatus.PLACED));

        InTouchRoom otherRoom = InTouchRoom.builder().id(11L).build();
        fixture.keys().add(key(
                otherRoom, fixture.group(), null, participant,
                LiveKeyAssignmentState.POOLED, LiveKeyBuildStatus.NOT_STARTED));
        InTouchRoomGroup otherGroup = InTouchRoomGroup.builder().id(21L).build();
        fixture.keys().add(key(
                fixture.room(), otherGroup, null, participant,
                LiveKeyAssignmentState.POOLED, LiveKeyBuildStatus.NOT_STARTED));

        LiveRoomParticipantAccessRowResponse row = rowFor(
                fixture.service().getParticipantAccess(ROOM_ID),
                participant.getId());

        assertThat(row.getReleasableUnfinishedKeyCount()).isEqualTo(1);
        assertThat(row.getReleasedToPoolKeyCount()).isEqualTo(2);
    }

    @Test
    void removeModeCountsOnlyAssignedInProgress() {
        Fixture fixture = fixture(LiveRoomBuildMode.REMOVE_KEYS);
        InTouchRoomParticipant participant = fixture.participant();
        fixture.keys().add(key(
                fixture.room(), fixture.group(), participant, null,
                LiveKeyAssignmentState.ASSIGNED, LiveKeyBuildStatus.IN_PROGRESS));
        fixture.keys().add(key(
                fixture.room(), fixture.group(), participant, null,
                LiveKeyAssignmentState.ASSIGNED, LiveKeyBuildStatus.REMOVED));
        fixture.keys().add(key(
                fixture.room(), fixture.group(), participant, null,
                LiveKeyAssignmentState.ASSIGNED, LiveKeyBuildStatus.NOT_STARTED));
        fixture.keys().add(key(
                fixture.room(), fixture.group(), null, participant,
                LiveKeyAssignmentState.POOLED, LiveKeyBuildStatus.IN_PROGRESS));

        LiveRoomParticipantAccessRowResponse row = rowFor(
                fixture.service().getParticipantAccess(ROOM_ID),
                participant.getId());

        assertThat(row.getReleasableUnfinishedKeyCount()).isEqualTo(1);
        assertThat(row.getReleasedToPoolKeyCount()).isEqualTo(1);
    }

    @Test
    void participantWithNoUnfinishedOrReleasedKeysReturnsZeroCounts() {
        Fixture fixture = fixture(LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY);
        fixture.keys().add(key(
                fixture.room(), fixture.group(), fixture.participant(), null,
                LiveKeyAssignmentState.ASSIGNED, LiveKeyBuildStatus.PLACED));

        LiveRoomParticipantAccessRowResponse row = rowFor(
                fixture.service().getParticipantAccess(ROOM_ID),
                fixture.participant().getId());

        assertThat(row.getReleasableUnfinishedKeyCount()).isZero();
        assertThat(row.getReleasedToPoolKeyCount()).isZero();
    }

    private LiveRoomParticipantAccessRowResponse rowFor(
            LiveRoomParticipantAccessResponse response,
            Long participantId
    ) {
        return response.getParticipants().stream()
                .filter(row -> row.getParticipantId().equals(participantId))
                .findFirst()
                .orElseThrow();
    }

    private Fixture fixture(LiveRoomBuildMode buildMode) {
        InTouchRoom room = InTouchRoom.builder()
                .id(ROOM_ID)
                .roomCode("ABC123")
                .title("Room")
                .owner(User.builder().id(OWNER_ID).build())
                .status(InTouchRoomStatus.STARTED)
                .buildMode(buildMode)
                .build();
        InTouchRoomGroup group = InTouchRoomGroup.builder()
                .id(GROUP_ID)
                .room(room)
                .name("Group")
                .build();
        InTouchRoomParticipant participant = InTouchRoomParticipant.builder()
                .id(30L)
                .room(room)
                .displayName("Participant")
                .participantCode("1234")
                .status(ParticipantStatus.LEFT)
                .build();
        InTouchRoomGroupParticipant assignment = InTouchRoomGroupParticipant.builder()
                .room(room)
                .group(group)
                .participant(participant)
                .build();
        List<InTouchRoomGroupLiveKey> keys = new ArrayList<>();

        InTouchRoomRepository roomRepository = proxy(
                InTouchRoomRepository.class,
                (name, args) -> name.equals("findById")
                        ? Optional.of(room)
                        : unexpected(name));
        InTouchRoomParticipantRepository participantRepository = proxy(
                InTouchRoomParticipantRepository.class,
                (name, args) -> name.equals("findByRoomIdWithMobileUser")
                        ? List.of(participant)
                        : unexpected(name));
        InTouchRoomGroupParticipantRepository assignmentRepository = proxy(
                InTouchRoomGroupParticipantRepository.class,
                (name, args) -> name.equals("findAssignmentsForParticipantAccess")
                        ? List.of(assignment)
                        : unexpected(name));
        InTouchRoomGroupLiveKeyRepository keyRepository = proxy(
                InTouchRoomGroupLiveKeyRepository.class,
                (name, args) -> {
                    if (name.equals(
                            "countByRoomIdAndGroupIdAndAssignedParticipantIdAndAssignmentStateAndStatus")) {
                        return keys.stream()
                                .filter(key -> key.getRoom().getId().equals(args[0]))
                                .filter(key -> key.getGroup().getId().equals(args[1]))
                                .filter(key -> key.getAssignedParticipant() != null)
                                .filter(key -> key.getAssignedParticipant().getId().equals(args[2]))
                                .filter(key -> key.getAssignmentState() == args[3])
                                .filter(key -> key.getStatus() == args[4])
                                .count();
                    }
                    if (name.equals("countByRoomIdAndGroupIdAndReleasedFromParticipantId")) {
                        return keys.stream()
                                .filter(key -> key.getRoom().getId().equals(args[0]))
                                .filter(key -> key.getGroup().getId().equals(args[1]))
                                .filter(key -> key.getReleasedFromParticipant() != null)
                                .filter(key -> key.getReleasedFromParticipant().getId()
                                        .equals(args[2]))
                                .count();
                    }
                    return unexpected(name);
                });
        SecurityUtils security = new SecurityUtils() {
            @Override
            public Integer getCurrentUserId() {
                return OWNER_ID;
            }
        };
        InTouchRoomOwnerQueryService service = new InTouchRoomOwnerQueryService(
                roomRepository,
                null,
                new InTouchRoomAccessValidator(security),
                keyRepository,
                assignmentRepository,
                participantRepository);
        return new Fixture(service, room, group, participant, keys);
    }

    private InTouchRoomGroupLiveKey key(
            InTouchRoom room,
            InTouchRoomGroup group,
            InTouchRoomParticipant assignedParticipant,
            InTouchRoomParticipant releasedFrom,
            LiveKeyAssignmentState assignmentState,
            LiveKeyBuildStatus status
    ) {
        return InTouchRoomGroupLiveKey.builder()
                .room(room)
                .group(group)
                .assignedParticipant(assignedParticipant)
                .releasedFromParticipant(releasedFrom)
                .assignmentState(assignmentState)
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
            InTouchRoomOwnerQueryService service,
            InTouchRoom room,
            InTouchRoomGroup group,
            InTouchRoomParticipant participant,
            List<InTouchRoomGroupLiveKey> keys
    ) {
    }
}
