package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.MobileJoinRoomResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InTouchRoomMobileControllerTest {

    @Test
    void currentRoomReturnsNoContentWhenNoAssociationExists() {
        InTouchRoomParticipantService participantService =
                new InTouchRoomParticipantService(null, null, null, null, null, null, null) {
                    @Override
                    public Optional<MobileJoinRoomResponse> getCurrentRoom() {
                        return Optional.empty();
                    }
                };
        InTouchRoomMobileController controller =
                new InTouchRoomMobileController(participantService, null, null);

        ResponseEntity<?> response = controller.getCurrentRoom();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void leaveCurrentRoomReturnsNoContent() {
        boolean[] leaveCalled = {false};
        InTouchRoomParticipantService participantService =
                new InTouchRoomParticipantService(null, null, null, null, null, null, null) {
                    @Override
                    public void leaveCurrentRoom() {
                        leaveCalled[0] = true;
                    }
                };
        InTouchRoomMobileController controller =
                new InTouchRoomMobileController(participantService, null, null);

        ResponseEntity<?> response = controller.leaveCurrentRoom();

        assertThat(leaveCalled[0]).isTrue();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }
}
