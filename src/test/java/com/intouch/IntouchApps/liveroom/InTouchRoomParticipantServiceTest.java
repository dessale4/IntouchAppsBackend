package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.MobileJoinRoomResponse;
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

class InTouchRoomParticipantServiceTest {

    private static final Integer USER_ID = 42;

    @Test
    void invalidParticipantCodeBehaviorIsUnchanged() {
        InTouchRoomParticipantRepository participantRepository = proxy(
                InTouchRoomParticipantRepository.class,
                (methodName, args) -> {
                    if (methodName.equals("findByRoomCodeAndParticipantCode")) {
                        return Optional.empty();
                    }
                    throw new UnsupportedOperationException(methodName);
                }
        );
        InTouchRoomParticipantService service = new InTouchRoomParticipantService(
                participantRepository,
                null,
                null,
                null,
                securityUtils(),
                null,
                null
        );

        assertThatThrownBy(() -> service.joinRoom("ABC123", "9999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid room code or participant code");
    }

    @Test
    void joinedParticipantInReadyRoomIsReturned() {
        InTouchRoomParticipantService service = serviceReturning(
                participant(ParticipantStatus.JOINED, false, InTouchRoomStatus.READY)
        );

        MobileJoinRoomResponse result = service.getCurrentRoom().orElseThrow();

        assertThat(result.getRoomStatus()).isEqualTo(InTouchRoomStatus.READY);
        assertThat(result.getParticipantStatus()).isEqualTo(ParticipantStatus.JOINED);
        assertThat(result.getCanPlay()).isFalse();
    }

    @Test
    void activeParticipantInStartedRoomIsReturned() {
        InTouchRoomParticipantService service = serviceReturning(
                participant(ParticipantStatus.ACTIVE, true, InTouchRoomStatus.STARTED)
        );

        MobileJoinRoomResponse result = service.getCurrentRoom().orElseThrow();

        assertThat(result.getRoomStatus()).isEqualTo(InTouchRoomStatus.STARTED);
        assertThat(result.getParticipantStatus()).isEqualTo(ParticipantStatus.ACTIVE);
        assertThat(result.getCanPlay()).isTrue();
    }

    @Test
    void activeParticipantInPausedRoomIsReturnedWithCanPlayFalse() {
        InTouchRoomParticipantService service = serviceReturning(
                participant(ParticipantStatus.ACTIVE, true, InTouchRoomStatus.PAUSED)
        );

        MobileJoinRoomResponse result = service.getCurrentRoom().orElseThrow();

        assertThat(result.getRoomStatus()).isEqualTo(InTouchRoomStatus.PAUSED);
        assertThat(result.getCanPlay()).isFalse();
    }

    @Test
    void completedRoomIsExcluded() {
        assertThat(serviceReturningNoAssociation().getCurrentRoom()).isEmpty();
    }

    @Test
    void cancelledRoomIsExcluded() {
        assertThat(serviceReturningNoAssociation().getCurrentRoom()).isEmpty();
    }

    @Test
    void leftParticipantIsExcluded() {
        assertThat(serviceReturningNoAssociation().getCurrentRoom()).isEmpty();
    }

    @Test
    void leavesJoinedParticipantInReadyRoomAndPreservesAssociationData() {
        assertSuccessfulLeave(ParticipantStatus.JOINED, false, InTouchRoomStatus.READY);
    }

    @Test
    void leavesActiveParticipantInStartedRoomAndPreservesProgress() {
        assertSuccessfulLeave(ParticipantStatus.ACTIVE, true, InTouchRoomStatus.STARTED);
    }

    @Test
    void leavesActiveParticipantInPausedRoom() {
        assertSuccessfulLeave(ParticipantStatus.ACTIVE, true, InTouchRoomStatus.PAUSED);
    }

    @Test
    void leaveWithoutCurrentAssociationReturnsBusinessError() {
        LeaveHarness harness = leaveHarness(List.of());

        org.assertj.core.api.Assertions.assertThatThrownBy(harness.service()::leaveCurrentRoom)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No current live room participation found.");

        assertThat(harness.repositoryCalls())
                .containsExactly("findCurrentResumableParticipants");
        assertThat(harness.publishedRoomIds()).isEmpty();
    }

    private void assertSuccessfulLeave(
            ParticipantStatus initialStatus,
            boolean initiallyActive,
            InTouchRoomStatus roomStatus
    ) {
        Instant claimedAt = Instant.parse("2026-07-01T10:15:30Z");
        Instant activatedAt = Instant.parse("2026-07-01T10:30:00Z");
        User mobileUser = User.builder().id(USER_ID).userName("mobile-user").build();
        InTouchRoomParticipant participant = participant(initialStatus, initiallyActive, roomStatus);
        participant.setMobileUser(mobileUser);
        participant.setClaimedAt(claimedAt);
        participant.setActivatedAt(activatedAt);
        String participantCode = participant.getParticipantCode();
        String displayName = participant.getDisplayName();
        InTouchRoom room = participant.getRoom();

        LeaveHarness harness = leaveHarness(List.of(participant));

        harness.service().leaveCurrentRoom();

        assertThat(participant.getStatus()).isEqualTo(ParticipantStatus.LEFT);
        assertThat(participant.getActiveInRoom()).isFalse();
        assertThat(participant.getMobileUser()).isSameAs(mobileUser);
        assertThat(participant.getClaimedAt()).isEqualTo(claimedAt);
        assertThat(participant.getActivatedAt()).isEqualTo(activatedAt);
        assertThat(participant.getParticipantCode()).isEqualTo(participantCode);
        assertThat(participant.getDisplayName()).isEqualTo(displayName);
        assertThat(participant.getRoom()).isSameAs(room);
        assertThat(room.getStatus()).isEqualTo(roomStatus);
        assertThat(harness.repositoryCalls())
                .containsExactly("findCurrentResumableParticipants", "save");
        assertThat(harness.publishedRoomIds()).containsExactly(10L);
    }

    private LeaveHarness leaveHarness(List<InTouchRoomParticipant> result) {
        List<String> repositoryCalls = new ArrayList<>();
        InTouchRoomParticipantRepository participantRepository = proxy(
                InTouchRoomParticipantRepository.class,
                (methodName, args) -> {
                    repositoryCalls.add(methodName);
                    if (methodName.equals("findCurrentResumableParticipants")) {
                        assertThat(args).containsExactly(USER_ID);
                        return result;
                    }
                    if (methodName.equals("save")) {
                        assertThat(args).containsExactly(result.get(0));
                        return args[0];
                    }
                    throw new AssertionError("Unexpected repository operation: " + methodName);
                }
        );
        List<Long> publishedRoomIds = new ArrayList<>();
        InTouchRoomProgressPublisher progressPublisher =
                new InTouchRoomProgressPublisher(null, null) {
                    @Override
                    public void publishRoomProgress(Long roomId) {
                        publishedRoomIds.add(roomId);
                    }
                };

        InTouchRoomParticipantService service = new InTouchRoomParticipantService(
                participantRepository,
                null,
                null,
                null,
                securityUtils(),
                null,
                progressPublisher
        );
        return new LeaveHarness(service, repositoryCalls, publishedRoomIds);
    }

    private InTouchRoomParticipantService serviceReturning(InTouchRoomParticipant participant) {
        return service(List.of(participant));
    }

    private InTouchRoomParticipantService serviceReturningNoAssociation() {
        return service(List.of());
    }

    private InTouchRoomParticipantService service(List<InTouchRoomParticipant> result) {
        InTouchRoomParticipantRepository participantRepository = proxy(
                InTouchRoomParticipantRepository.class,
                (methodName, args) -> {
                    if (methodName.equals("findCurrentResumableParticipants")) {
                        assertThat(args).containsExactly(USER_ID);
                        return result;
                    }
                    throw new UnsupportedOperationException(methodName);
                }
        );
        InTouchRoomGroupParticipantRepository groupParticipantRepository = proxy(
                InTouchRoomGroupParticipantRepository.class,
                (methodName, args) -> {
                    if (methodName.equals("findByRoomIdAndParticipantId")) {
                        return List.of();
                    }
                    throw new UnsupportedOperationException(methodName);
                }
        );
        return new InTouchRoomParticipantService(
                participantRepository,
                null,
                null,
                null,
                securityUtils(),
                groupParticipantRepository,
                null
        );
    }

    @Test
    void duplicateCurrentAssociationsReturnControlledConflict() {
        InTouchRoomParticipant first = participant(
                ParticipantStatus.ACTIVE, true, InTouchRoomStatus.STARTED);
        InTouchRoomParticipant second = participant(
                ParticipantStatus.JOINED, false, InTouchRoomStatus.READY);
        second.setId(21L);
        second.getRoom().setId(11L);

        assertThatThrownBy(() -> service(List.of(first, second)).getCurrentRoom())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Multiple current live-room participations found. Please contact support."
                );
    }

    private SecurityUtils securityUtils() {
        return new SecurityUtils() {
            @Override
            public Integer getCurrentUserId() {
                return USER_ID;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> invocation.call(method.getName(), args)
        );
    }

    private InTouchRoomParticipant participant(
            ParticipantStatus participantStatus,
            boolean activeInRoom,
            InTouchRoomStatus roomStatus
    ) {
        InTouchRoom room = InTouchRoom.builder()
                .id(10L)
                .title("Current room")
                .roomCode("ABC123")
                .status(roomStatus)
                .buildMode(LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY)
                .paidRoom(false)
                .build();

        return InTouchRoomParticipant.builder()
                .id(20L)
                .room(room)
                .participantCode("1234")
                .displayName("Mobile participant")
                .status(participantStatus)
                .activeInRoom(activeInRoom)
                .build();
    }

    @FunctionalInterface
    private interface Invocation {
        Object call(String methodName, Object[] args);
    }

    private record LeaveHarness(
            InTouchRoomParticipantService service,
            List<String> repositoryCalls,
            List<Long> publishedRoomIds
    ) {
    }
}
