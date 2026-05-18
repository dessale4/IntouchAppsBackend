package com.intouch.IntouchApps.liveroom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InTouchRoomLiveKeyFamilyRepository
        extends JpaRepository<InTouchRoomLiveKeyFamily, Long> {

    List<InTouchRoomLiveKeyFamily> findByRoomIdAndActiveTrueOrderByRowIndexAsc(Long roomId);

    void deleteByRoomId(Long roomId);
}
