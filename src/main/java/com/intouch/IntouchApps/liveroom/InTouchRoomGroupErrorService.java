package com.intouch.IntouchApps.liveroom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InTouchRoomGroupErrorService {

    private final InTouchRoomGroupRepository groupRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increaseGroupErrorCount(Long groupId) {
        InTouchRoomGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found."));

        group.setErrorCount((group.getErrorCount() == null ? 0 : group.getErrorCount()) + 1);

        groupRepository.save(group);
    }
}
