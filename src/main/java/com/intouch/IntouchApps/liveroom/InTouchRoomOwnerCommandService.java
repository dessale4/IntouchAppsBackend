package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.*;
import com.intouch.IntouchApps.liveroom.dto.response.*;
import com.intouch.IntouchApps.liveroom.repository.InTouchRoomGroupBoardRowRepository;
import com.intouch.IntouchApps.liveroom.withPattern.InTouchRoomBoardPattern;
import com.intouch.IntouchApps.liveroom.withPattern.InTouchRoomBoardPatternCellRepository;
import com.intouch.IntouchApps.liveroom.withPattern.InTouchRoomBoardPatternRepository;
import com.intouch.IntouchApps.liveroom.withPattern.InTouchRoomGroupPatternProgressRepository;
import com.intouch.IntouchApps.security.SecurityUtils;
import com.intouch.IntouchApps.user.User;
import com.intouch.IntouchApps.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InTouchRoomOwnerCommandService {

    private final InTouchRoomRepository roomRepository;
    private final InTouchRoomGroupRepository groupRepository;
    private final InTouchRoomParticipantRepository participantRepository;
    private final InTouchRoomGroupParticipantRepository groupParticipantRepository;
    private final InTouchRoomLiveKeyFamilyRepository liveKeyFamilyRepository;
    private final InTouchRoomLiveKeyRepository liveKeyRepository;
    private final InTouchRoomCodeService codeService;
    private final InTouchRoomAccessValidator accessValidator;
    private final InTouchRoomLifecycleValidator lifecycleValidator;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final InTouchRoomMapper mapper;
    private final InTouchRoomGroupLiveKeyRepository groupLiveKeyRepository;
    private final InTouchRoomGroupBoardRowRepository boardRowRepository;
    private final InTouchRoomProgressPublisher progressPublisher;
    private final InTouchRoomBoardPatternCellRepository boardPatternCellRepository;
    private final InTouchRoomGroupPatternProgressRepository groupPatternProgressRepository;
    private final InTouchRoomBoardPatternRepository boardPatternRepository;

    @Transactional
    public InTouchRoom createRoom(CreateRoomRequest request) {
        Integer currentUserId = securityUtils.getCurrentUserId();
        boolean titleExists =
                roomRepository.existsByOwnerIdAndTitleIgnoreCaseAndDeletedFalse(
                        currentUserId,
                        request.getTitle().trim()
                );

        if (titleExists) {
            throw new IllegalStateException("You already have a live room with this title.");
        }
        InTouchRoom room = InTouchRoom.builder()
                .roomCode(codeService.generateUniqueRoomCode())
                .title(request.getTitle().trim())
                .buildMode(request.getBuildMode())
                .placementStrategy(request.getPlacementStrategy())
                .shuffleKeys(request.getShuffleKeys())
                .scoringEnabled(request.getScoringEnabled())
                .targetStructure(request.getTargetStructure())
                .status(InTouchRoomStatus.DRAFT)
                .owner(userRepository.getReferenceById(currentUserId))
                .build();

        return roomRepository.save(room);
    }

    @Transactional
    public void removeParticipantSlot(Long roomId, Long participantId) {
        InTouchRoom room = getEditableOwnerRoom(roomId);

        InTouchRoomParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found."));

        if (!participant.getRoom().getId().equals(roomId)) {
            throw new IllegalStateException("Participant does not belong to this room.");
        }

        List<InTouchRoomGroupParticipant> assignments =
                groupParticipantRepository.findByRoomIdAndParticipantId(
                        roomId,
                        participantId
                );

        if (!assignments.isEmpty()) {
            groupParticipantRepository.deleteAll(assignments);
        }
//        participant.setStatus(ParticipantStatus.REMOVED);
//        participant.setActiveInRoom(false);
//        participantRepository.save(participant);
        participantRepository.delete(participant);
    }

    @Transactional
    public List<LiveRoomGroupResponse> addGroupsBulk(
            Long roomId,
            BulkCreateGroupsRequest request
    ) {
        InTouchRoom room = getEditableOwnerRoom(roomId);
        int requestedCount = request.getCount();
        Long existingGroupCount = existingGroupCount(requestedCount, room.getId());
        List<InTouchRoomGroup> groups = new ArrayList<>();

        for (int i = 1; i <= requestedCount; i++) {
            String groupName = request.getBaseName().trim() + " " + (existingGroupCount + i);

            if (groupRepository.existsByRoomIdAndNameIgnoreCase(roomId, groupName)) {
                throw new IllegalStateException("Group name already exists: " + groupName);
            }

            groups.add(InTouchRoomGroup.builder()
                    .room(room)
                    .name(groupName)
                    .sortOrder(existingGroupCount.intValue() + i)
                    .build());
        }

        return groupRepository.saveAll(groups)
                .stream()
                .map(mapper::toGroupResponse)
                .toList();
    }

    private Long existingGroupCount(Integer requestedCount, Long roomId) {
        Integer currentUserId = securityUtils.getCurrentUserId();
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        long existingGroupCount = groupRepository.countByRoomId(roomId);
        int maxAllowed = owner.getMaxLiveRoomGroupCount() == null
                ? 5
                : owner.getMaxLiveRoomGroupCount();

        if (existingGroupCount + requestedCount > maxAllowed) {
            throw new IllegalStateException(
                    "Group limit exceeded. Your current limit is " + maxAllowed + "."
            );
        }
        return existingGroupCount;
    }

    @Transactional(readOnly = true)
    public List<LiveRoomResponse> getMyRooms() {
        Integer currentUserId = securityUtils.getCurrentUserId();

        return roomRepository
                .findByOwnerIdAndDeletedFalseOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(mapper::toRoomResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LiveRoomSetupResponse getRoomSetup(Long roomId) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        accessValidator.ensureRoomOwnerOrAdmin(room);

        List<LiveRoomGroupResponse> groups =
                groupRepository.findByRoomIdOrderBySortOrderAsc(roomId)
                        .stream()
                        .map(mapper::toGroupResponse)
                        .toList();

        List<LiveRoomParticipantResponse> participants =
                participantRepository.findByRoomIdAndStatusNot(roomId, ParticipantStatus.REMOVED)
                        .stream()
                        .map(mapper::toParticipantResponse)
                        .toList();

        List<LiveRoomAssignmentResponse> assignments =
                groupParticipantRepository.findRoomAssignments(roomId)
                        .stream()
                        .map(this::toAssignmentResponse)
                        .toList();
        List<LiveRoomTemplateFamilyResponse> templateFamilies =
                liveKeyFamilyRepository
                        .findByRoomIdAndActiveTrueOrderByRowIndexAsc(roomId)
                        .stream()
                        .map(family -> LiveRoomTemplateFamilyResponse.builder()
                                .key(family.getFamilyCode())
                                .name(family.getFamilyName())
                                .build())
                        .toList();
        return LiveRoomSetupResponse.builder()
                .room(mapper.toRoomResponse(room))
                .groups(groups)
                .participants(participants)
                .assignments(assignments)
                .templateFamilies(templateFamilies)
                .build();
    }

    private LiveRoomAssignmentResponse toAssignmentResponse(
            InTouchRoomGroupParticipant assignment
    ) {
        return LiveRoomAssignmentResponse.builder()
                .id(assignment.getId())
                .participantId(assignment.getParticipant().getId())
                .participantDisplayName(assignment.getParticipant().getDisplayName())
                .groupId(assignment.getGroup().getId())
                .groupName(assignment.getGroup().getName())
                .build();
    }

    @Transactional
    public InTouchRoomGroup addGroup(Long roomId, CreateGroupRequest request) {
        InTouchRoom room = getEditableOwnerRoom(roomId);
        Long existingGroupCount = existingGroupCount(1, roomId);
        if (groupRepository.existsByRoomIdAndNameIgnoreCase(roomId, request.getName())) {
            throw new IllegalStateException("Group name already exists in this room.");
        }

        InTouchRoomGroup group = InTouchRoomGroup.builder()
                .room(room)
                .name(request.getName())
                .sortOrder(request.getSortOrder())
                .build();

        return groupRepository.save(group);
    }

    @Transactional
    public InTouchRoomParticipant addParticipantSlot(
            Long roomId,
            AddParticipantSlotRequest request
    ) {
        InTouchRoom room = getEditableOwnerRoom(roomId);
        Long existingParticipantCount = existingParticipantCount(1, roomId);
        boolean exists = participantRepository
                .existsByRoomIdAndDisplayNameIgnoreCaseAndStatusNot(
                        roomId,
                        request.getDisplayName().trim(),
                        ParticipantStatus.REMOVED
                );

        if (exists) {
            throw new IllegalStateException(
                    "Participant name already exists in this room."
            );
        }
        InTouchRoomParticipant participant = InTouchRoomParticipant.builder()
                .room(room)
                .participantCode(codeService.generateUniqueParticipantCode(roomId))
                .displayName(request.getDisplayName().trim())
                .status(ParticipantStatus.INVITED)
                .activeInRoom(false)
                .build();

        return participantRepository.save(participant);
    }

    @Transactional
    public void assignParticipantToGroup(
            Long roomId,
            AssignParticipantToGroupRequest request
    ) {
        InTouchRoom room = getEditableOwnerRoom(roomId);

        InTouchRoomParticipant participant = participantRepository.findById(request.getParticipantId())
                .orElseThrow(() -> new IllegalArgumentException("Participant not found."));

        InTouchRoomGroup group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found."));

        if (!participant.getRoom().getId().equals(roomId)) {
            throw new IllegalStateException("Participant does not belong to this room.");
        }

        if (!group.getRoom().getId().equals(roomId)) {
            throw new IllegalStateException("Group does not belong to this room.");
        }

        List<InTouchRoomGroupParticipant> existingAssignments =
                groupParticipantRepository.findByRoomIdAndParticipantId(
                        roomId,
                        participant.getId()
                );

        if (existingAssignments.isEmpty()) {
            groupParticipantRepository.save(
                    InTouchRoomGroupParticipant.builder()
                            .room(room)
                            .participant(participant)
                            .group(group)
                            .build()
            );
        } else {
            InTouchRoomGroupParticipant mainAssignment = existingAssignments.get(0);
            mainAssignment.setGroup(group);
            groupParticipantRepository.save(mainAssignment);

            if (existingAssignments.size() > 1) {
                groupParticipantRepository.deleteAll(
                        existingAssignments.subList(1, existingAssignments.size())
                );
            }
        }
    }

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

        groupPatternProgressRepository.deleteByRoomId(roomId);
        boardPatternCellRepository.deleteByPatternRoomId(roomId);
        boardPatternRepository.deleteByRoomId(roomId);

        groupLiveKeyRepository.deleteByRoomId(roomId);
        boardRowRepository.deleteByRoomId(roomId);
        groupParticipantRepository.deleteByRoomId(roomId);
        participantRepository.deleteByRoomId(roomId);
        groupRepository.deleteByRoomId(roomId);
        liveKeyRepository.deleteByRoomId(roomId);
        liveKeyFamilyRepository.deleteByRoomId(roomId);

        roomRepository.delete(room);
    }

    @Transactional
    public void pauseRoom(Long roomId) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        accessValidator.ensureRoomOwnerOrAdmin(room);
        lifecycleValidator.ensureCanPause(room);

        room.setStatus(InTouchRoomStatus.PAUSED);
        roomRepository.save(room);

        progressPublisher.publishRoomProgress(roomId);
    }

    @Transactional
    public void resumeRoom(Long roomId) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        accessValidator.ensureRoomOwnerOrAdmin(room);
        lifecycleValidator.ensureCanResume(room);


        room.setStatus(InTouchRoomStatus.STARTED);
        roomRepository.save(room);

        progressPublisher.publishRoomProgress(roomId);
    }

    @Transactional
    public void createTemplate(
            Long roomId,
            CreateLiveRoomTemplateRequest request
    ) {
        InTouchRoom room = getEditableOwnerRoom(roomId);

        liveKeyRepository.deleteByRoomId(roomId);
        liveKeyFamilyRepository.deleteByRoomId(roomId);

        for (LiveKeyFamilyRequest familyRequest : request.getFamilies()) {
            InTouchRoomLiveKeyFamily family =
                    InTouchRoomLiveKeyFamily.builder()
                            .room(room)
                            .familyCode(familyRequest.getFamilyCode())
                            .familyName(familyRequest.getFamilyName())
                            .rowIndex(familyRequest.getRowIndex())
                            .columnCount(familyRequest.getColumnCount())
                            .keyType(familyRequest.getKeyType())
                            .active(true)
                            .build();

            InTouchRoomLiveKeyFamily savedFamily =
                    liveKeyFamilyRepository.save(family);

            if (familyRequest.getKeys() == null) {
                continue;
            }

            List<InTouchRoomLiveKey> keys = familyRequest.getKeys().stream()
                    .map(keyRequest -> InTouchRoomLiveKey.builder()
                            .room(room)
                            .family(savedFamily)
                            .keyValue(keyRequest.getKeyValue())
                            .keyType(keyRequest.getKeyType())
                            .keyFamilyId(keyRequest.getKeyFamilyId())
                            .targetRow(keyRequest.getTargetRow())
                            .targetColumn(keyRequest.getTargetColumn())
                            .sortOrder(keyRequest.getSortOrder())
                            .active(true)
                            .build())
                    .toList();

            liveKeyRepository.saveAll(keys);
        }
    }

    @Transactional
    public List<LiveRoomParticipantResponse> addParticipantSlotsBulk(
            Long roomId,
            BulkCreateParticipantSlotsRequest request
    ) {
        InTouchRoom room = getEditableOwnerRoom(roomId);
        int requestedCount = request.getCount();

        Long existingParticipantCount = existingParticipantCount(requestedCount, room.getId());
        List<InTouchRoomParticipant> participants = new ArrayList<>();

        for (int i = 1; i <= requestedCount; i++) {
            String displayName =
                    request.getBaseName().trim() + " " + (existingParticipantCount + i);

            if (participantRepository.existsByRoomIdAndDisplayNameIgnoreCaseAndStatusNot(
                    roomId,
                    displayName,
                    ParticipantStatus.REMOVED
            )) {
                throw new IllegalStateException(
                        "Participant name already exists: " + displayName
                );
            }

            participants.add(InTouchRoomParticipant.builder()
                    .room(room)
                    .participantCode(codeService.generateUniqueParticipantCode(roomId))
                    .displayName(displayName)
                    .status(ParticipantStatus.INVITED)
                    .activeInRoom(false)
                    .build());
        }

        return participantRepository.saveAll(participants)
                .stream()
                .map(mapper::toParticipantResponse)
                .toList();
    }

    private Long existingParticipantCount(Integer requestedCount, Long roomId) {
        Integer currentUserId = securityUtils.getCurrentUserId();
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        long existingParticipantCount =
                participantRepository.countByRoomIdAndStatusNot(
                        roomId,
                        ParticipantStatus.REMOVED
                );

        int maxAllowed = owner.getMaxLiveRoomParticipantCount() == null
                ? 20
                : owner.getMaxLiveRoomParticipantCount();

        if (existingParticipantCount + requestedCount > maxAllowed) {
            throw new IllegalStateException(
                    "Participant limit exceeded. Your current limit is " + maxAllowed + "."
            );
        }
        return existingParticipantCount;
    }

    private InTouchRoom getEditableOwnerRoom(Long roomId) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        accessValidator.ensureRoomOwnerOrAdmin(room);
        lifecycleValidator.ensureEditableBeforeStart(room);

        return room;
    }

    @Transactional
    public void deleteGroup(Long roomId, Long groupId) {
        InTouchRoom room = getEditableOwnerRoom(roomId);

        InTouchRoomGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found."));

        if (!group.getRoom().getId().equals(roomId)) {
            throw new IllegalStateException("Group does not belong to this room.");
        }

        groupParticipantRepository.deleteByRoomIdAndGroupId(roomId, groupId);
        groupRepository.delete(group);
    }

    @Transactional
    public void resetRoom(Long roomId) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        accessValidator.ensureRoomOwnerOrAdmin(room);
        lifecycleValidator.ensureCanReset(room);

        groupLiveKeyRepository.deleteByRoomId(roomId);
        boardRowRepository.deleteByRoomId(roomId);
        groupPatternProgressRepository.deleteByRoomId(roomId);

        groupRepository.resetProgressByRoomId(roomId);
        participantRepository.resetParticipantsAfterRoomReset(roomId);

        room.setStatus(InTouchRoomStatus.DRAFT);
        room.setStartedAt(null);
        room.setCompletedAt(null);
        room.setReplayCount(
                room.getReplayCount() == null ? 1 : room.getReplayCount() + 1
        );
        roomRepository.save(room);

        progressPublisher.publishRoomProgress(roomId);
    }

    @Transactional
    public void assignParticipantsEvenly(Long roomId) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        accessValidator.ensureRoomOwnerOrAdmin(room);
        lifecycleValidator.ensureEditableBeforeStart(room);

        List<InTouchRoomGroup> groups =
                groupRepository.findByRoomIdOrderBySortOrderAsc(roomId);

        if (groups.isEmpty()) {
            throw new IllegalStateException("Create at least one group first.");
        }

        List<InTouchRoomParticipant> participants =
                participantRepository.findByRoomIdOrderByIdAsc(roomId);

        if (participants.isEmpty()) {
            throw new IllegalStateException("Create participants first.");
        }
        if (groups.size() > participants.size()) {
            throw new IllegalStateException(
                    "Cannot assign evenly because there are more groups than participants. Add more participants or reduce the number of groups."
            );
        }
        List<InTouchRoomParticipant> shuffledParticipants =
                new ArrayList<>(participants);

        Collections.shuffle(shuffledParticipants, new SecureRandom());

        List<InTouchRoomGroupParticipant> existingAssignments =
                groupParticipantRepository.findByRoomId(roomId);

        groupParticipantRepository.deleteAll(existingAssignments);
        groupParticipantRepository.flush();

        List<InTouchRoomGroupParticipant> newAssignments = new ArrayList<>();

        for (int i = 0; i < shuffledParticipants.size(); i++) {
            InTouchRoomGroup group = groups.get(i % groups.size());
            InTouchRoomParticipant participant = shuffledParticipants.get(i);

            newAssignments.add(
                    InTouchRoomGroupParticipant.builder()
                            .room(room)
                            .group(group)
                            .participant(participant)
                            .build()
            );
        }

        groupParticipantRepository.saveAll(newAssignments);
    }

    @Transactional
    public void releaseParticipantClaim(Long roomId, Long participantId) {
 InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        accessValidator.ensureRoomOwner(room);

        InTouchRoomParticipant participant =
                participantRepository.findByIdAndRoomId(participantId, roomId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Participant not found")
                        );
        lifecycleValidator.ensureCanReleaseParticipantClaim(room, participant);

        participant.setMobileUser(null);
        participant.setClaimedAt(null);
        participant.setStatus(ParticipantStatus.INVITED);
        participantRepository.save(participant);
        progressPublisher.publishRoomProgress(room.getId());
    }
}
