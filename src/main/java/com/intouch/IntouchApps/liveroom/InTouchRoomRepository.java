package com.intouch.IntouchApps.liveroom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InTouchRoomRepository extends JpaRepository<InTouchRoom, Long> {
    boolean existsByRoomCode(String roomCode);
    List<InTouchRoom> findByOwnerIdAndDeletedFalseOrderByCreatedAtDesc(Integer ownerId);

    boolean existsByOwnerIdAndTitleIgnoreCaseAndDeletedFalse(
            Integer ownerId,
            String title
    );
}


