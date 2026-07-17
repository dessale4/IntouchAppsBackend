package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.CurrentParticipantKeyDto;
import com.intouch.IntouchApps.liveroom.dto.response.InTouchRoomGroupProgressDto;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InTouchRoomProgressServiceTest {

    @Test
    void progressIncludesParticipantStatusWithoutChangingCurrentKeyFields() throws Exception {
        InTouchRoom room = InTouchRoom.builder()
                .id(10L)
                .buildMode(LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY)
                .build();
        InTouchRoomGroup group = InTouchRoomGroup.builder()
                .id(20L)
                .name("Group A")
                .build();
        InTouchRoomParticipant left = participant(30L, "Left user", "1111", ParticipantStatus.LEFT);
        InTouchRoomParticipant active = participant(31L, "Active user", "2222", ParticipantStatus.ACTIVE);
        List<InTouchRoomGroupParticipant> assignments = List.of(
                assignment(room, group, left),
                assignment(room, group, active)
        );

        InTouchRoomGroupParticipantRepository assignmentRepository = proxy(
                InTouchRoomGroupParticipantRepository.class,
                (name, args) -> name.equals("findByRoomIdAndGroupId")
                        ? assignments
                        : unexpected(name)
        );
        InTouchRoomGroupLiveKeyRepository keyRepository = proxy(
                InTouchRoomGroupLiveKeyRepository.class,
                (name, args) -> {
                    if (name.equals("countByRoomIdAndGroupId")) return 2L;
                    if (name.equals("countByRoomIdAndGroupIdAndStatus")) return 0L;
                    if (name.equals("findFirstByRoom_IdAndGroup_IdAndAssignedParticipant_IdAndStatusOrderByAssignedOrderAsc")) {
                        Long participantId = (Long) args[2];
                        return Optional.of(InTouchRoomGroupLiveKey.builder()
                                .keyValue(participantId.equals(30L) ? "L" : "A")
                                .assignedOrder(participantId.equals(30L) ? 4 : 5)
                                .build());
                    }
                    return unexpected(name);
                }
        );
        InTouchRoomProgressService service = new InTouchRoomProgressService(
                null,
                null,
                keyRepository,
                assignmentRepository
        );

        Method calculate = InTouchRoomProgressService.class.getDeclaredMethod(
                "calculateGroupProgress",
                InTouchRoom.class,
                InTouchRoomGroup.class
        );
        calculate.setAccessible(true);
        InTouchRoomGroupProgressDto result =
                (InTouchRoomGroupProgressDto) calculate.invoke(service, room, group);

        assertThat(result.getCurrentParticipantKeys()).containsExactly(
                new CurrentParticipantKeyDto(30L, "Left user", "1111", ParticipantStatus.LEFT, "L", 4),
                new CurrentParticipantKeyDto(31L, "Active user", "2222", ParticipantStatus.ACTIVE, "A", 5)
        );
    }

    private InTouchRoomParticipant participant(
            Long id,
            String name,
            String code,
            ParticipantStatus status
    ) {
        return InTouchRoomParticipant.builder()
                .id(id)
                .displayName(name)
                .participantCode(code)
                .status(status)
                .build();
    }

    private InTouchRoomGroupParticipant assignment(
            InTouchRoom room,
            InTouchRoomGroup group,
            InTouchRoomParticipant participant
    ) {
        return InTouchRoomGroupParticipant.builder()
                .room(room)
                .group(group)
                .participant(participant)
                .build();
    }

    private Object unexpected(String name) {
        throw new AssertionError("Unexpected repository operation: " + name);
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, args) -> invocation.call(method.getName(), args)
        );
    }

    private interface Invocation {
        Object call(String name, Object[] args);
    }
}
