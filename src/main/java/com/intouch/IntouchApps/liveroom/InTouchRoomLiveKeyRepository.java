package com.intouch.IntouchApps.liveroom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InTouchRoomLiveKeyRepository
        extends JpaRepository<InTouchRoomLiveKey, Long> {

    List<InTouchRoomLiveKey> findByRoomIdAndActiveTrueOrderBySortOrderAsc(Long roomId);

    long countByRoomIdAndActiveTrue(Long roomId);

    void deleteByRoomId(Long roomId);
}
