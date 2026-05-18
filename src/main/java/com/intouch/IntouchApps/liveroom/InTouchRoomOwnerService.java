package com.intouch.IntouchApps.liveroom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InTouchRoomOwnerService {

    private final InTouchRoomRepository roomRepository;
    private final InTouchRoomAccessValidator accessValidator;
    private final InTouchRoomLifecycleValidator lifecycleValidator;
    private final InTouchRoomProgressPublisher progressPublisher;

    @Transactional
    public void cancelRoom(Long roomId) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        accessValidator.ensureRoomOwnerOrAdmin(room);
        lifecycleValidator.ensureCanCancel(room);

        room.setStatus(InTouchRoomStatus.CANCELLED);

        roomRepository.save(room);
        progressPublisher.publishRoomProgress(roomId);
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        accessValidator.ensureRoomOwnerOrAdmin(room);
        lifecycleValidator.ensureCanDelete(room);

        room.setDeleted(true);
        room.setStatus(InTouchRoomStatus.DELETED);

        roomRepository.save(room);
    }
}
