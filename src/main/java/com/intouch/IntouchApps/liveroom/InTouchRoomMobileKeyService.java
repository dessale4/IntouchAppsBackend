package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.request.MobilePlaceKeyRequest;
import com.intouch.IntouchApps.liveroom.dto.request.MobileRemoveKeyRequest;
import com.intouch.IntouchApps.liveroom.dto.response.MobileBoardCellResponse;
import com.intouch.IntouchApps.liveroom.dto.response.MobileBoardRowResponse;
import com.intouch.IntouchApps.liveroom.dto.response.MobileMyBoardResponse;
import com.intouch.IntouchApps.liveroom.dto.response.MobileNextKeyResponse;
import com.intouch.IntouchApps.liveroom.entity.InTouchRoomGroupBoardRow;
import com.intouch.IntouchApps.liveroom.repository.InTouchRoomGroupBoardRowRepository;
import com.intouch.IntouchApps.liveroom.withPattern.InTouchRoomPatternScoringService;
import com.intouch.IntouchApps.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InTouchRoomMobileKeyService {

    private final InTouchRoomGroupLiveKeyRepository groupLiveKeyRepository;
    private final InTouchRoomGroupRepository groupRepository;
    private final InTouchRoomProgressPublisher progressPublisher;
    private final SecurityUtils securityUtils;
    private final InTouchRoomPatternScoringService patternScoringService;
    private final InTouchRoomLifecycleValidator lifecycleValidator;
    private final InTouchRoomCompletionService completionService;
    private final InTouchRoomGroupErrorService groupErrorService;
    private final InTouchRoomGroupBoardRowRepository boardRowRepository;
    private final InTouchRoomPooledKeyClaimService pooledKeyClaimService;

    @Transactional
    public MobileNextKeyResponse placeKey(
            Long groupLiveKeyId,
            MobilePlaceKeyRequest request
    ) {
        InTouchRoomGroupLiveKey selectedKey =
                findKeyForCurrentParticipant(groupLiveKeyId);

        InTouchRoom room = selectedKey.getRoom();
        lifecycleValidator.ensureGameplayAllowed(room);

        InTouchRoomGroup group = selectedKey.getGroup();
        InTouchRoomParticipant participant = selectedKey.getAssignedParticipant();

        if (room.getStatus() != InTouchRoomStatus.STARTED) {
            throw new IllegalStateException("Live room is not started.");
        }

        if (selectedKey.getStatus() != LiveKeyBuildStatus.NOT_STARTED) {
            throw new IllegalStateException("This key has already been completed.");
        }

    /*
     Rule 1:
     Participant must place the key in its correct column.

     Example:
     ሀ belongs to column 1.
     If user taps column 2, it is wrong.
    */
        Integer resolvedRowIndex;

        try {
            resolvedRowIndex = Integer.parseInt(selectedKey.getKeyFamilyId());
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(
                    "Invalid key family id for row resolution."
            );
        }

        if (!selectedKey.getTargetColumn().equals(request.getColumnIndex())) {
            groupErrorService.increaseGroupErrorCount(group.getId());
            publishProgress(room.getId());
            throw new IllegalStateException("Wrong column selected.");
        }
    /*
     Rule 2:
     Find the selected group board row.

     If it does not exist yet, create it.
    */
        InTouchRoomGroupBoardRow boardRow =
                boardRowRepository
                        .findByRoomIdAndGroupIdAndRowIndex(
                                room.getId(),
                                group.getId(),
                                resolvedRowIndex
                        )
                        .orElseGet(() -> InTouchRoomGroupBoardRow.builder()
                                .room(room)
                                .group(group)
                                .rowIndex(resolvedRowIndex)
                                .createdByParticipant(participant)
                                .createdAt(Instant.now())
                                .build());

    /*
     Rule 3:
     If the row is already locked to a key family,
     the selected key must belong to the same key family.

    */
        if (boardRow.getKeyFamilyId() != null &&
                !boardRow.getKeyFamilyId().equals(selectedKey.getKeyFamilyId())) {
            groupErrorService.increaseGroupErrorCount(group.getId());
            publishProgress(room.getId());

            throw new IllegalStateException(
                    "This row is already locked to another key family."
            );
        }

    /*
     Rule 4:
     If the row is still empty/unlocked,
     first correct placement locks the row to this key's family.
    */
        if (boardRow.getKeyFamilyId() == null) {
            boardRow.setKeyFamilyId(selectedKey.getKeyFamilyId());
        }

        boardRowRepository.save(boardRow);

    /*
     Rule 5:
     Save actual placement.
     The target row no longer controls user thinking.
     Resolved key-family row becomes currentRow.
     User-selected column becomes currentColumn.
    */
        selectedKey.setCurrentRow(resolvedRowIndex);
        selectedKey.setCurrentColumn(request.getColumnIndex());
        selectedKey.setStatus(LiveKeyBuildStatus.PLACED);
        selectedKey.setPlacedAt(Instant.now());

        groupLiveKeyRepository.save(selectedKey);

        patternScoringService.evaluatePatternsAfterPlacement(
                room,
                group,
                resolvedRowIndex,
                request.getColumnIndex()
        );

        completionService.updateCompletionStatus(room.getId());

        publishProgress(room.getId());

        return pooledKeyClaimService.nextKeyAfterSuccessfulWork(room.getId());
    }

    @Transactional
    public MobileNextKeyResponse removeKey(
            Long targetGroupLiveKeyId,
            MobileRemoveKeyRequest request
    ) {
        InTouchRoomGroupLiveKey targetKey =
                findKeyForCurrentParticipant(targetGroupLiveKeyId);

        InTouchRoom room = targetKey.getRoom();
        InTouchRoomGroup group = targetKey.getGroup();
        InTouchRoomParticipant participant = targetKey.getAssignedParticipant();

        lifecycleValidator.ensureGameplayAllowed(room);
        lifecycleValidator.ensureParticipantCanUpdate(room);

        if (room.getStatus() != InTouchRoomStatus.STARTED) {
            throw new IllegalStateException("Live room is not started.");
        }

        if (room.getBuildMode() != LiveRoomBuildMode.REMOVE_KEYS) {
            throw new IllegalStateException("Room is not in REMOVE_KEYS mode.");
        }

        if (targetKey.getAssignmentState() != LiveKeyAssignmentState.ASSIGNED ||
                targetKey.getStatus() != LiveKeyBuildStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Only an assigned in-progress key can be removed."
            );
        }

        InTouchRoomGroupLiveKey clickedKey =
                groupLiveKeyRepository.findById(request.getClickedGroupLiveKeyId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("Clicked key not found.")
                        );

        boolean sameRoom =
                clickedKey.getRoom().getId().equals(room.getId());

        boolean sameGroup =
                clickedKey.getGroup().getId().equals(group.getId());

        if (!sameRoom || !sameGroup) {
            groupErrorService.increaseGroupErrorCount(group.getId());
            publishProgress(room.getId());
            throw new IllegalStateException("Invalid key selected.");
        }

        boolean clickedAssignedToParticipant =
                clickedKey.getAssignedParticipant() != null &&
                        clickedKey.getAssignedParticipant().getId()
                                .equals(participant.getId());

        boolean clickedIsTarget =
                clickedKey.getId().equals(targetKey.getId());

        boolean clickedAlreadyRemoved =
                clickedKey.getStatus() == LiveKeyBuildStatus.REMOVED;

        if (!clickedIsTarget || !clickedAssignedToParticipant || clickedAlreadyRemoved) {
            groupErrorService.increaseGroupErrorCount(group.getId());
            publishProgress(room.getId());
            throw new IllegalStateException("Wrong key selected.");
        }

        clickedKey.setStatus(LiveKeyBuildStatus.REMOVED);
        clickedKey.setRemovedByParticipant(participant);
        clickedKey.setRemovedAt(Instant.now());

        groupLiveKeyRepository.save(clickedKey);

        completionService.updateCompletionStatus(room.getId());

        publishProgress(room.getId());

        return pooledKeyClaimService.nextKeyAfterSuccessfulWork(room.getId());
    }
    private void placeByExactTargetPosition(InTouchRoomGroupLiveKey key, Integer requestedRow, Integer requestedColumn) {
        if (requestedRow == null || requestedColumn == null) {
            groupErrorService.increaseGroupErrorCount(key.getGroup().getId());
            publishProgress(key.getRoom().getId());
            throw new IllegalStateException("Row and column are required.");
        }

        if (!requestedRow.equals(key.getTargetRow()) || !requestedColumn.equals(key.getTargetColumn())) {

            groupErrorService.increaseGroupErrorCount(key.getGroup().getId());
            publishProgress(key.getRoom().getId());
            throw new IllegalStateException("Wrong key placement.");
        }

        if (groupLiveKeyRepository.existsPlacedKeyAtCell(key.getRoom().getId(), key.getGroup().getId(), requestedRow, requestedColumn)) {
            groupErrorService.increaseGroupErrorCount(key.getGroup().getId());
            publishProgress(key.getRoom().getId());
            throw new IllegalStateException("This cell is already occupied.");
        }

        try {
            key.setCurrentRow(requestedRow);
            key.setCurrentColumn(requestedColumn);
            key.setStatus(LiveKeyBuildStatus.PLACED);
            groupLiveKeyRepository.save(key);
            patternScoringService.evaluatePatternsAfterPlacement(key.getRoom(), key.getGroup(), requestedRow, requestedColumn);
        } catch (DataIntegrityViolationException ex) {
            groupErrorService.increaseGroupErrorCount(key.getGroup().getId());
            publishProgress(key.getRoom().getId());
            throw new IllegalStateException("This cell was just occupied by another participant.");
        }
    }

    private void placeByFirstAvailableMatchingValue(InTouchRoomGroupLiveKey selectedKey) {
        List<InTouchRoomGroupLiveKey> availableTargets = groupLiveKeyRepository.findFirstAvailableMatchingValueTarget(selectedKey.getRoom().getId(), selectedKey.getGroup().getId(), selectedKey.getKeyValue());

        if (availableTargets.isEmpty()) {
            groupErrorService.increaseGroupErrorCount(selectedKey.getGroup().getId());
            publishProgress(selectedKey.getRoom().getId());
            throw new IllegalStateException("No available target position for this key.");
        }

        InTouchRoomGroupLiveKey targetSlot = availableTargets.get(0);

        try {
            selectedKey.setCurrentRow(targetSlot.getTargetRow());
            selectedKey.setCurrentColumn(targetSlot.getTargetColumn());
            selectedKey.setStatus(LiveKeyBuildStatus.PLACED);
            groupLiveKeyRepository.save(selectedKey);
            patternScoringService.evaluatePatternsAfterPlacement(selectedKey.getRoom(), selectedKey.getGroup(), targetSlot.getTargetRow(), targetSlot.getTargetColumn());
        } catch (DataIntegrityViolationException ex) {
            retryPlaceByFirstAvailableMatchingValue(selectedKey);
        }
    }

    private void retryPlaceByFirstAvailableMatchingValue(InTouchRoomGroupLiveKey selectedKey) {
        List<InTouchRoomGroupLiveKey> availableTargets = groupLiveKeyRepository.findFirstAvailableMatchingValueTarget(selectedKey.getRoom().getId(), selectedKey.getGroup().getId(), selectedKey.getKeyValue());

        if (availableTargets.isEmpty()) {
            groupErrorService.increaseGroupErrorCount(selectedKey.getGroup().getId());
            publishProgress(selectedKey.getRoom().getId());
            throw new IllegalStateException("No available target position for this key.");
        }

        InTouchRoomGroupLiveKey targetSlot = availableTargets.get(0);

        selectedKey.setCurrentRow(targetSlot.getTargetRow());
        selectedKey.setCurrentColumn(targetSlot.getTargetColumn());
        selectedKey.setStatus(LiveKeyBuildStatus.PLACED);

        groupLiveKeyRepository.save(selectedKey);
        patternScoringService.evaluatePatternsAfterPlacement(selectedKey.getRoom(), selectedKey.getGroup(), targetSlot.getTargetRow(), targetSlot.getTargetColumn());
    }
private InTouchRoomGroupLiveKey findKeyForCurrentParticipant(
        Long groupLiveKeyId
) {
    Integer currentUserId = securityUtils.getCurrentUserId();

    InTouchRoomGroupLiveKey key =
            groupLiveKeyRepository
                    .findAssignedKeyForCurrentUser(
                            groupLiveKeyId,
                            currentUserId
                    )
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Key not found or not assigned to current participant."
                    ));

    lifecycleValidator.ensureGameplayAllowed(key.getRoom());

    InTouchRoomParticipant participant = key.getAssignedParticipant();

    if (participant == null ||
            participant.getMobileUser() == null ||
            !participant.getMobileUser().getId().equals(currentUserId)) {
        throw new IllegalStateException(
                "This key is not assigned to the current participant."
        );
    }

    if (participant.getStatus() != ParticipantStatus.ACTIVE ||
            !Boolean.TRUE.equals(participant.getActiveInRoom())) {
        throw new IllegalStateException(
                "Participant is not active in this room."
        );
    }

    return key;
}

    private void publishProgress(Long roomId) {
        progressPublisher.publishRoomProgress(roomId);
    }
}
