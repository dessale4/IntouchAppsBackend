package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.request.MobilePlaceKeyRequest;
import com.intouch.IntouchApps.liveroom.dto.request.MobileRemoveKeyRequest;
import com.intouch.IntouchApps.liveroom.dto.response.MobileNextKeyResponse;
import com.intouch.IntouchApps.liveroom.repository.InTouchRoomGroupBoardRowRepository;
import com.intouch.IntouchApps.liveroom.withPattern.InTouchRoomPatternScoringService;
import com.intouch.IntouchApps.security.SecurityUtils;
import com.intouch.IntouchApps.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InTouchRoomMobileKeyServicePooledClaimRoutingTest {

    private static final Long ROOM_ID = 10L;
    private static final Long KEY_ID = 20L;
    private static final Integer USER_ID = 30;

    @Test
    void successfulPlaceUsesSharedPostWorkClaimPath() {
        Fixture fixture = fixture(LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY);

        MobileNextKeyResponse response = fixture.service().placeKey(
                KEY_ID,
                MobilePlaceKeyRequest.builder().columnIndex(1).build());

        assertThat(response.getRoomId()).isEqualTo(ROOM_ID);
        assertThat(fixture.claimCalls()).containsExactly(ROOM_ID);
        assertThat(fixture.key().getStatus()).isEqualTo(LiveKeyBuildStatus.PLACED);
    }

    @Test
    void successfulRemoveUsesSharedPostWorkClaimPath() {
        Fixture fixture = fixture(LiveRoomBuildMode.REMOVE_KEYS);

        MobileNextKeyResponse response = fixture.service().removeKey(
                KEY_ID,
                new MobileRemoveKeyRequest(KEY_ID));

        assertThat(response.getRoomId()).isEqualTo(ROOM_ID);
        assertThat(fixture.claimCalls()).containsExactly(ROOM_ID);
        assertThat(fixture.key().getStatus()).isEqualTo(LiveKeyBuildStatus.REMOVED);
    }

    @Test
    void activeParticipantMustAlsoBeActiveInRoomForPlaceAndRemove() {
        Fixture place = fixture(LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY, false);
        assertThatThrownBy(() -> place.service().placeKey(
                KEY_ID, MobilePlaceKeyRequest.builder().columnIndex(1).build()))
                .hasMessage("Participant is not active in this room.");

        Fixture remove = fixture(LiveRoomBuildMode.REMOVE_KEYS, false);
        assertThatThrownBy(() -> remove.service().removeKey(
                KEY_ID, new MobileRemoveKeyRequest(KEY_ID)))
                .hasMessage("Participant is not active in this room.");
    }

    @Test
    void logicallyDeletedStartedRoomRejectsPlaceAndRemoveConsistently() {
        Fixture place = fixture(LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY);
        place.key().getRoom().setDeleted(true);
        assertThatThrownBy(() -> place.service().placeKey(
                KEY_ID, MobilePlaceKeyRequest.builder().columnIndex(1).build()))
                .hasMessage("Deleted room cannot be used for gameplay.");

        Fixture remove = fixture(LiveRoomBuildMode.REMOVE_KEYS);
        remove.key().getRoom().setDeleted(true);
        assertThatThrownBy(() -> remove.service().removeKey(
                KEY_ID, new MobileRemoveKeyRequest(KEY_ID)))
                .hasMessage("Deleted room cannot be used for gameplay.");
    }

    @ParameterizedTest
    @EnumSource(LiveKeyBuildStatus.class)
    void removeRequiresAssignedInProgressKey(LiveKeyBuildStatus status) {
        Fixture fixture = fixture(LiveRoomBuildMode.REMOVE_KEYS);
        fixture.key().setStatus(status);

        if (status == LiveKeyBuildStatus.IN_PROGRESS) {
            assertThatCode(() -> fixture.service().removeKey(
                    KEY_ID, new MobileRemoveKeyRequest(KEY_ID)))
                    .doesNotThrowAnyException();
        } else {
            assertThatThrownBy(() -> fixture.service().removeKey(
                    KEY_ID, new MobileRemoveKeyRequest(KEY_ID)))
                    .hasMessage("Only an assigned in-progress key can be removed.");
        }
    }

    @Test
    void pooledInProgressKeyCannotBeRemoved() {
        Fixture fixture = fixture(LiveRoomBuildMode.REMOVE_KEYS);
        fixture.key().setAssignmentState(LiveKeyAssignmentState.POOLED);

        assertThatThrownBy(() -> fixture.service().removeKey(
                KEY_ID, new MobileRemoveKeyRequest(KEY_ID)))
                .hasMessage("Only an assigned in-progress key can be removed.");
    }

    private Fixture fixture(LiveRoomBuildMode buildMode) {
        return fixture(buildMode, true);
    }

    private Fixture fixture(LiveRoomBuildMode buildMode, boolean activeInRoom) {
        InTouchRoom room = InTouchRoom.builder()
                .id(ROOM_ID)
                .status(InTouchRoomStatus.STARTED)
                .buildMode(buildMode)
                .scoringEnabled(false)
                .build();
        InTouchRoomGroup group = InTouchRoomGroup.builder().id(40L).room(room).build();
        InTouchRoomParticipant participant = InTouchRoomParticipant.builder()
                .id(50L)
                .room(room)
                .mobileUser(User.builder().id(USER_ID).build())
                .status(ParticipantStatus.ACTIVE)
                .activeInRoom(activeInRoom)
                .build();
        InTouchRoomGroupLiveKey key = InTouchRoomGroupLiveKey.builder()
                .id(KEY_ID)
                .room(room)
                .group(group)
                .assignedParticipant(participant)
                .assignmentState(LiveKeyAssignmentState.ASSIGNED)
                .keyValue("A")
                .keyType(LiveKeyType.CUSTOM)
                .keyFamilyId("1")
                .targetRow(1)
                .targetColumn(1)
                .currentRow(buildMode == LiveRoomBuildMode.REMOVE_KEYS ? 1 : null)
                .currentColumn(buildMode == LiveRoomBuildMode.REMOVE_KEYS ? 1 : null)
                .status(buildMode == LiveRoomBuildMode.REMOVE_KEYS
                        ? LiveKeyBuildStatus.IN_PROGRESS
                        : LiveKeyBuildStatus.NOT_STARTED)
                .build();

        InTouchRoomGroupLiveKeyRepository keyRepository = proxy(
                InTouchRoomGroupLiveKeyRepository.class,
                (name, args) -> {
                    if (name.equals("findAssignedKeyForCurrentUser") ||
                            name.equals("findById")) {
                        return Optional.of(key);
                    }
                    if (name.equals("save")) return args[0];
                    return unexpected(name);
                });
        InTouchRoomGroupBoardRowRepository boardRepository = proxy(
                InTouchRoomGroupBoardRowRepository.class,
                (name, args) -> {
                    if (name.equals("findByRoomIdAndGroupIdAndRowIndex")) {
                        return Optional.empty();
                    }
                    if (name.equals("save")) return args[0];
                    return unexpected(name);
                });
        List<Long> claimCalls = new ArrayList<>();
        InTouchRoomPooledKeyClaimService claimService =
                new InTouchRoomPooledKeyClaimService(
                        null, null, null, null, null, null, null, null) {
                    @Override
                    public MobileNextKeyResponse nextKeyAfterSuccessfulWork(Long roomId) {
                        claimCalls.add(roomId);
                        return MobileNextKeyResponse.builder().roomId(roomId).build();
                    }
                };
        SecurityUtils security = new SecurityUtils() {
            @Override
            public Integer getCurrentUserId() {
                return USER_ID;
            }
        };
        InTouchRoomCompletionService completionService =
                new InTouchRoomCompletionService(null, null, null) {
                    @Override
                    public void updateCompletionStatus(Long roomId) {
                    }
                };
        InTouchRoomProgressPublisher publisher = new InTouchRoomProgressPublisher(null, null) {
            @Override
            public void publishRoomProgress(Long roomId) {
            }
        };

        InTouchRoomMobileKeyService service = new InTouchRoomMobileKeyService(
                keyRepository,
                null,
                publisher,
                security,
                new InTouchRoomPatternScoringService(null, null, null),
                new InTouchRoomLifecycleValidator(),
                completionService,
                null,
                boardRepository,
                claimService);
        return new Fixture(service, key, claimCalls);
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
            InTouchRoomMobileKeyService service,
            InTouchRoomGroupLiveKey key,
            List<Long> claimCalls
    ) {
    }
}
