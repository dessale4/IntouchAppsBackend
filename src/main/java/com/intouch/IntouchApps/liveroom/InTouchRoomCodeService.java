package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.utils.RandomCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InTouchRoomCodeService {

    private static final int ROOM_CODE_LENGTH = 6;
    private static final int PARTICIPANT_CODE_LENGTH = 4;
    private static final int MAX_ATTEMPTS = 20;

    private final InTouchRoomRepository roomRepository;
    private final InTouchRoomParticipantRepository participantRepository;

    public String generateUniqueRoomCode() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = RandomCodeGenerator.alphaNumericCode(ROOM_CODE_LENGTH);

            if (!roomRepository.existsByRoomCode(code)) {
                return code;
            }
        }

        throw new IllegalStateException("Unable to generate unique room code");
    }

    public String generateUniqueParticipantCode(Long roomId) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = RandomCodeGenerator.numericCode(PARTICIPANT_CODE_LENGTH);

            if (!participantRepository.existsByRoomIdAndParticipantCode(roomId, code)) {
                return code;
            }
        }

        throw new IllegalStateException("Unable to generate unique participant code");
    }
}
