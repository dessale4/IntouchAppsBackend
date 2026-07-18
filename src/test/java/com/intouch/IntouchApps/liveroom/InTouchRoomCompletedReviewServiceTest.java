package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.InTouchRoomGroupProgressDto;
import com.intouch.IntouchApps.liveroom.dto.response.InTouchRoomProgressDto;
import com.intouch.IntouchApps.liveroom.dto.response.MobileMyBoardResponse;
import com.intouch.IntouchApps.security.SecurityUtils;
import com.intouch.IntouchApps.user.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InTouchRoomCompletedReviewServiceTest {

    private static final Long ROOM_ID = 10L;
    private static final Long PARTICIPANT_ID = 20L;
    private static final Long GROUP_ID = 30L;
    private static final Integer USER_ID = 42;
    private static final String ROOM_CODE = "A7K9P2";

    @Test
    void returnsOnlyTheAuthenticatedParticipantsCompletedRoomAndGroup() {
        InTouchRoom room = room(InTouchRoomStatus.COMPLETED, false);
        InTouchRoomParticipant participant = participant(room);
        InTouchRoomGroup group = InTouchRoomGroup.builder()
                .id(GROUP_ID).room(room).name("Group A").build();
        InTouchRoomGroupProgressDto groupProgress = InTouchRoomGroupProgressDto.builder()
                .groupId(GROUP_ID).groupName("Group A").score(95).errorCount(2).build();
        MobileMyBoardResponse board = MobileMyBoardResponse.builder()
                .roomId(ROOM_ID).rows(List.of()).build();

        var service = service(
                room,
                participant,
                List.of(InTouchRoomGroupParticipant.builder()
                        .room(room).participant(participant).group(group).build()),
                board,
                InTouchRoomProgressDto.builder().groups(List.of(groupProgress)).build()
        );

        var response = service.getCompletedReview(ROOM_CODE);

        assertThat(response.getRoomStatus()).isEqualTo(InTouchRoomStatus.COMPLETED);
        assertThat(response.getParticipantDisplayName()).isEqualTo("Participant");
        assertThat(response.getGroupName()).isEqualTo("Group A");
        assertThat(response.getBoard()).isSameAs(board);
        assertThat(response.getProgress()).isSameAs(groupProgress);
    }

    @Test
    void rejectsAUserWhoDoesNotOwnTheParticipantRecord() {
        var service = service(
                room(InTouchRoomStatus.COMPLETED, false),
                null,
                List.of(),
                null,
                null
        );

        assertThatThrownBy(() -> service.getCompletedReview(ROOM_CODE))
                .hasMessage("You are not assigned to this live room.");
    }

    @Test
    void rejectsRoomsThatAreNotCompleted() {
        var service = service(
                room(InTouchRoomStatus.STARTED, false), null, List.of(), null, null);

        assertThatThrownBy(() -> service.getCompletedReview(ROOM_CODE))
                .hasMessage("Only completed rooms can be reviewed.");
    }

    @Test
    void rejectsDeletedCompletedRooms() {
        var service = service(
                room(InTouchRoomStatus.COMPLETED, true), null, List.of(), null, null);

        assertThatThrownBy(() -> service.getCompletedReview(ROOM_CODE))
                .hasMessage("Deleted room cannot be reviewed.");
    }

    private InTouchRoomCompletedReviewService service(
            InTouchRoom room,
            InTouchRoomParticipant participant,
            List<InTouchRoomGroupParticipant> assignments,
            MobileMyBoardResponse board,
            InTouchRoomProgressDto progress
    ) {
        InTouchRoomRepository roomRepository = proxy(InTouchRoomRepository.class,
                (name, args) -> name.equals("findByRoomCode")
                        ? Optional.of(room)
                        : unexpected(name));
        InTouchRoomParticipantRepository participantRepository = proxy(
                InTouchRoomParticipantRepository.class,
                (name, args) -> name.equals("findByRoomIdAndMobileUserId")
                        ? Optional.ofNullable(participant)
                        : unexpected(name));
        InTouchRoomGroupParticipantRepository assignmentRepository = proxy(
                InTouchRoomGroupParticipantRepository.class,
                (name, args) -> name.equals("findByRoomIdAndParticipantId")
                        ? assignments
                        : unexpected(name));
        InTouchRoomMobileQueryService queryService =
                new InTouchRoomMobileQueryService(null, null, null, null, null) {
                    @Override
                    MobileMyBoardResponse getCompletedReviewBoard(InTouchRoom room) {
                        return board;
                    }
                };
        InTouchRoomProgressService progressService =
                new InTouchRoomProgressService(null, null, null, null) {
                    @Override
                    public InTouchRoomProgressDto getRoomProgress(Long roomId) {
                        return progress;
                    }
                };
        SecurityUtils securityUtils = new SecurityUtils() {
            @Override
            public Integer getCurrentUserId() {
                return USER_ID;
            }
        };

        return new InTouchRoomCompletedReviewService(
                roomRepository,
                participantRepository,
                assignmentRepository,
                queryService,
                progressService,
                securityUtils
        );
    }

    private InTouchRoom room(InTouchRoomStatus status, boolean deleted) {
        return InTouchRoom.builder()
                .id(ROOM_ID)
                .roomCode(ROOM_CODE)
                .title("Room")
                .buildMode(LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY)
                .status(status)
                .deleted(deleted)
                .build();
    }

    private InTouchRoomParticipant participant(InTouchRoom room) {
        return InTouchRoomParticipant.builder()
                .id(PARTICIPANT_ID)
                .room(room)
                .mobileUser(User.builder().id(USER_ID).build())
                .displayName("Participant")
                .status(ParticipantStatus.ACTIVE)
                .activeInRoom(true)
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
}
