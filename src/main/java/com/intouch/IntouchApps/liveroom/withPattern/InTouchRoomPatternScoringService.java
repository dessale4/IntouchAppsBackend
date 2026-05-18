package com.intouch.IntouchApps.liveroom.withPattern;

import com.intouch.IntouchApps.liveroom.InTouchRoom;
import com.intouch.IntouchApps.liveroom.InTouchRoomGroup;
import com.intouch.IntouchApps.liveroom.InTouchRoomGroupLiveKeyRepository;
import com.intouch.IntouchApps.liveroom.LiveKeyBuildStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InTouchRoomPatternScoringService {

    private final InTouchRoomBoardPatternCellRepository patternCellRepository;
    private final InTouchRoomGroupPatternProgressRepository patternProgressRepository;
    private final InTouchRoomGroupLiveKeyRepository groupLiveKeyRepository;

    @Transactional
    public void evaluatePatternsAfterPlacement(
            InTouchRoom room,
            InTouchRoomGroup group,
            Integer placedRow,
            Integer placedColumn
    ) {
        if (!Boolean.TRUE.equals(room.getScoringEnabled())) {
            return;
        }

        List<InTouchRoomBoardPatternCell> affectedPatternCells =
                patternCellRepository.findActivePatternCellsForRoomCell(
                        room.getId(),
                        placedRow,
                        placedColumn
                );

        for (InTouchRoomBoardPatternCell patternCell : affectedPatternCells) {
            InTouchRoomBoardPattern pattern = patternCell.getPattern();

            InTouchRoomGroupPatternProgress progress =
                    patternProgressRepository
                            .findByGroupIdAndPatternId(group.getId(), pattern.getId())
                            .orElseGet(() -> InTouchRoomGroupPatternProgress.builder()
                                    .room(room)
                                    .group(group)
                                    .pattern(pattern)
                                    .completed(false)
                                    .earnedPoints(0)
                                    .build());

            if (Boolean.TRUE.equals(progress.getCompleted())) {
                continue;
            }

            boolean patternCompleted = isPatternCompleted(group.getId(), pattern.getId());

            if (patternCompleted) {
                progress.setCompleted(true);
                progress.setCompletedAt(Instant.now());
                progress.setEarnedPoints(pattern.getPoints());

                group.setScore((group.getScore() == null ? 0 : group.getScore()) + pattern.getPoints());
                group.setCompletedPatternCount(
                        (group.getCompletedPatternCount() == null ? 0 : group.getCompletedPatternCount()) + 1
                );

                patternProgressRepository.save(progress);
            }
        }
    }

    private boolean isPatternCompleted(Long groupId, Long patternId) {
        List<InTouchRoomBoardPatternCell> cells =
                patternCellRepository.findByPatternId(patternId);

        for (InTouchRoomBoardPatternCell cell : cells) {
            boolean placed = groupLiveKeyRepository.existsByGroupIdAndCurrentRowAndCurrentColumnAndStatus(
                    groupId,
                    cell.getTargetRow(),
                    cell.getTargetColumn(),
                    LiveKeyBuildStatus.PLACED
            );

            if (!placed) {
                return false;
            }
        }

        return true;
    }
}