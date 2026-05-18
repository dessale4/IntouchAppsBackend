package com.intouch.IntouchApps.liveroom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InTouchRoomStartService {

    private final InTouchRoomRepository roomRepository;
    private final InTouchRoomGroupRepository groupRepository;
    private final InTouchRoomLiveKeyRepository liveKeyRepository;
    private final InTouchRoomGroupParticipantRepository groupParticipantRepository;
    private final InTouchRoomGroupLiveKeyRepository groupLiveKeyRepository;
    private final InTouchRoomParticipantRepository participantRepository;
    private final InTouchRoomStartValidator startValidator;
    private final InTouchRoomProgressPublisher progressPublisher;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final InTouchRoomAccessValidator accessValidator;
    private final InTouchRoomLifecycleValidator lifecycleValidator;

    @Transactional
    public void startRoom(Long roomId) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));
        accessValidator.ensureRoomOwnerOrAdmin(room);
        lifecycleValidator.ensureCanStart(room);
        startValidator.validateCanStart(room);

        List<InTouchRoomLiveKey> templateKeys =
                liveKeyRepository.findByRoomIdAndActiveTrueOrderBySortOrderAsc(roomId);

        List<InTouchRoomGroup> groups =
                groupRepository.findByRoomIdOrderBySortOrderAsc(roomId);

        for (InTouchRoomGroup group : groups) {
            List<InTouchRoomParticipant> groupParticipants =
                    groupParticipantRepository.findParticipantsByGroupId(group.getId());

            if (groupParticipants.isEmpty()) {
                throw new IllegalStateException(
                        "Group " + group.getName() + " has no participants."
                );
            }

            createGroupLiveKeys(room, group, templateKeys, groupParticipants);
        }

        activateParticipants(roomId);

        room.setStatus(InTouchRoomStatus.STARTED);

        roomRepository.save(room);

        progressPublisher.publishRoomProgress(roomId);
    }

    private void createGroupLiveKeys(
            InTouchRoom room,
            InTouchRoomGroup group,
            List<InTouchRoomLiveKey> templateKeys,
            List<InTouchRoomParticipant> participants
    ) {
        List<InTouchRoomLiveKey> keysForAssignment = new ArrayList<>(templateKeys);

        if (Boolean.TRUE.equals(room.getShuffleKeys())) {
            Collections.shuffle(keysForAssignment, SECURE_RANDOM);
        }

        List<InTouchRoomGroupLiveKey> groupLiveKeys = new ArrayList<>();

        for (int i = 0; i < keysForAssignment.size(); i++) {
            InTouchRoomLiveKey sourceKey = keysForAssignment.get(i);

            InTouchRoomParticipant assignedParticipant =
                    participants.get(i % participants.size());

            InTouchRoomGroupLiveKey groupLiveKey = InTouchRoomGroupLiveKey.builder()
                    .room(room)
                    .group(group)
                    .sourceLiveKey(sourceKey)
                    .assignedParticipant(assignedParticipant)
                    .keyValue(sourceKey.getKeyValue())
                    .keyType(sourceKey.getKeyType())
                    .keyFamilyId(sourceKey.getKeyFamilyId())
                    .assignedOrder(i)
                    .currentRow(null)
                    .currentColumn(null)
                    .targetRow(sourceKey.getTargetRow())
                    .targetColumn(sourceKey.getTargetColumn())
                    .status(LiveKeyBuildStatus.NOT_STARTED)
                    .build();

            groupLiveKeys.add(groupLiveKey);
        }

        groupLiveKeyRepository.saveAll(groupLiveKeys);
    }

    private void activateParticipants(Long roomId) {
        List<InTouchRoomParticipant> participants =
                participantRepository.findByRoomIdAndStatusNot(roomId, ParticipantStatus.REMOVED);

        Instant now = Instant.now();

        for (InTouchRoomParticipant participant : participants) {
            if (participant.getMobileUser() == null) {
                throw new IllegalStateException(
                        "Cannot activate unclaimed participant: " +
                                participant.getDisplayName() +
                                " (" + participant.getParticipantCode() + ")"
                );
            }
            participant.setStatus(ParticipantStatus.ACTIVE);
            participant.setActiveInRoom(true);
            participant.setActivatedAt(now);
        }
        participantRepository.saveAll(participants);
    }

}