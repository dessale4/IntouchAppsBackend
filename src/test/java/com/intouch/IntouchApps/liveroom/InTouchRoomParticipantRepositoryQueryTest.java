package com.intouch.IntouchApps.liveroom;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import static org.assertj.core.api.Assertions.assertThat;

class InTouchRoomParticipantRepositoryQueryTest {

    @Test
    void currentRoomQueryContainsOnlyTheResumableLifecycleCombinations() throws Exception {
        Query query = InTouchRoomParticipantRepository.class
                .getMethod("findCurrentResumableParticipant", Integer.class)
                .getAnnotation(Query.class);

        assertThat(query).isNotNull();
        assertThat(query.value())
                .contains("p.status = 'JOINED'")
                .contains("p.activeInRoom = false")
                .contains("r.status IN ('DRAFT', 'READY')")
                .contains("p.status = 'ACTIVE'")
                .contains("p.activeInRoom = true")
                .contains("r.status IN ('STARTED', 'PAUSED')")
                .doesNotContain("'INVITED'")
                .doesNotContain("'LEFT'")
                .doesNotContain("'REMOVED'")
                .doesNotContain("'COMPLETED'")
                .doesNotContain("'CANCELLED'")
                .doesNotContain("'DELETED'");
    }

    @Test
    void otherRoomJoinRestrictionUsesTheSamePairedLifecycleDefinition()
            throws Exception {
        Query query = InTouchRoomParticipantRepository.class
                .getMethod(
                        "existsActiveParticipantInOtherActiveRoom",
                        Integer.class,
                        Long.class
                )
                .getAnnotation(Query.class);

        assertThat(query).isNotNull();
        assertThat(query.value())
                .contains("r.id <> :currentRoomId")
                .contains("p.status = 'JOINED'")
                .contains("p.activeInRoom = false")
                .contains("r.status IN ('DRAFT', 'READY')")
                .contains("p.status = 'ACTIVE'")
                .contains("p.activeInRoom = true")
                .contains("r.status IN ('STARTED', 'PAUSED')")
                .doesNotContain("'LEFT'")
                .doesNotContain("'REMOVED'")
                .doesNotContain("'INVITED'")
                .doesNotContain("'COMPLETED'")
                .doesNotContain("'CANCELLED'")
                .doesNotContain("'DELETED'");
    }
}
