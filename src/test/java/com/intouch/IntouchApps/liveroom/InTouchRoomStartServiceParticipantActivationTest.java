package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.user.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InTouchRoomStartServiceParticipantActivationTest {

    @Test
    void startActivatesJoinedParticipantButNeverLeftParticipant() throws Exception {
        InTouchRoomParticipant joined = InTouchRoomParticipant.builder()
                .id(1L).status(ParticipantStatus.JOINED).activeInRoom(false)
                .mobileUser(User.builder().id(42).build()).build();
        InTouchRoomParticipant left = InTouchRoomParticipant.builder()
                .id(2L).status(ParticipantStatus.LEFT).activeInRoom(false).build();
        InTouchRoomParticipantRepository repository =
                (InTouchRoomParticipantRepository) Proxy.newProxyInstance(
                        InTouchRoomParticipantRepository.class.getClassLoader(),
                        new Class<?>[]{InTouchRoomParticipantRepository.class},
                        (proxy, method, args) -> {
                            if (method.getName().equals("findByRoomIdAndStatus")) {
                                assertThat(args).containsExactly(10L, ParticipantStatus.JOINED);
                                return List.of(joined);
                            }
                            if (method.getName().equals("saveAll")) {
                                assertThat(args[0]).isEqualTo(List.of(joined));
                                return List.of(joined);
                            }
                            throw new AssertionError("Unexpected call: " + method.getName());
                        });
        InTouchRoomStartService service = new InTouchRoomStartService(
                null, null, null, null, null, repository, null, null, null, null);

        Method activate = InTouchRoomStartService.class
                .getDeclaredMethod("activateParticipants", Long.class);
        activate.setAccessible(true);
        activate.invoke(service, 10L);

        assertThat(joined.getStatus()).isEqualTo(ParticipantStatus.ACTIVE);
        assertThat(joined.getActiveInRoom()).isTrue();
        assertThat(joined.getActivatedAt()).isNotNull();
        assertThat(left.getStatus()).isEqualTo(ParticipantStatus.LEFT);
        assertThat(left.getActiveInRoom()).isFalse();
    }
}
