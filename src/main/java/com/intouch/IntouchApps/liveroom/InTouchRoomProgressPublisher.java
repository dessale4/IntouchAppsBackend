package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.InTouchRoomProgressDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InTouchRoomProgressPublisher {

    private final SimpMessagingTemplate messagingTemplate;
    private final InTouchRoomProgressService progressService;

    public void publishRoomProgress(Long roomId) {
        InTouchRoomProgressDto progress = progressService.getRoomProgress(roomId);

        messagingTemplate.convertAndSend(
                "/topic/intouch-rooms/" + roomId + "/progress",
                progress
        );
    }
}
