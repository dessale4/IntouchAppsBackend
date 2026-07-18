package com.intouch.IntouchApps.liveroom;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InTouchRoomGroupLiveKeyAssignmentStateTest {

    @Test
    void newEntityDefaultsToAssignedWithNullableProvenance() {
        InTouchRoomGroupLiveKey key = new InTouchRoomGroupLiveKey();

        assertThat(key.getAssignmentState()).isEqualTo(LiveKeyAssignmentState.ASSIGNED);
        assertThat(key.getReleasedFromParticipant()).isNull();
        assertThat(key.getPooledAt()).isNull();
    }

    @Test
    void builderDefaultsToAssignedWithNullableProvenance() {
        InTouchRoomGroupLiveKey key = InTouchRoomGroupLiveKey.builder().build();

        assertThat(key.getAssignmentState()).isEqualTo(LiveKeyAssignmentState.ASSIGNED);
        assertThat(key.getReleasedFromParticipant()).isNull();
        assertThat(key.getPooledAt()).isNull();
    }

    @Test
    void assignmentStateAndProvenanceUseStablePersistenceMappings() throws Exception {
        Field assignmentState = InTouchRoomGroupLiveKey.class.getDeclaredField("assignmentState");
        Enumerated enumerated = assignmentState.getAnnotation(Enumerated.class);
        Column assignmentColumn = assignmentState.getAnnotation(Column.class);

        assertThat(enumerated.value()).isEqualTo(EnumType.STRING);
        assertThat(assignmentColumn.name()).isEqualTo("assignment_state");
        assertThat(assignmentColumn.nullable()).isFalse();

        Field releasedFrom = InTouchRoomGroupLiveKey.class
                .getDeclaredField("releasedFromParticipant");
        assertThat(releasedFrom.getAnnotation(JoinColumn.class).name())
                .isEqualTo("released_from_participant_id");

        Field pooledAt = InTouchRoomGroupLiveKey.class.getDeclaredField("pooledAt");
        assertThat(pooledAt.getAnnotation(Column.class).name()).isEqualTo("pooled_at");
    }

    @Test
    void existingBuildModeStartKeyCreationSavesEveryKeyAsAssigned() throws Exception {
        assertStartKeyCreationDefaults(
                LiveRoomBuildMode.RECONSTRUCT_KEY_FAMILY,
                LiveKeyBuildStatus.NOT_STARTED,
                false);
    }

    @Test
    void existingRemoveModeStartKeyCreationSavesEveryKeyAsAssigned() throws Exception {
        assertStartKeyCreationDefaults(
                LiveRoomBuildMode.REMOVE_KEYS,
                LiveKeyBuildStatus.IN_PROGRESS,
                true);
    }

    private void assertStartKeyCreationDefaults(
            LiveRoomBuildMode buildMode,
            LiveKeyBuildStatus expectedStatus,
            boolean expectCurrentPosition
    ) throws Exception {
        List<InTouchRoomGroupLiveKey> saved = new ArrayList<>();
        InTouchRoomGroupLiveKeyRepository repository =
                (InTouchRoomGroupLiveKeyRepository) Proxy.newProxyInstance(
                        InTouchRoomGroupLiveKeyRepository.class.getClassLoader(),
                        new Class<?>[]{InTouchRoomGroupLiveKeyRepository.class},
                        (proxy, method, args) -> {
                            if (method.getName().equals("saveAll")) {
                                ((Iterable<?>) args[0]).forEach(item ->
                                        saved.add((InTouchRoomGroupLiveKey) item));
                                return saved;
                            }
                            throw new AssertionError("Unexpected call: " + method.getName());
                        });

        InTouchRoomStartService service = new InTouchRoomStartService(
                null, null, null, null, repository, null, null, null, null, null);
        InTouchRoom room = InTouchRoom.builder()
                .buildMode(buildMode)
                .shuffleKeys(false)
                .build();
        InTouchRoomGroup group = InTouchRoomGroup.builder().build();
        InTouchRoomParticipant firstParticipant = InTouchRoomParticipant.builder().build();
        InTouchRoomParticipant secondParticipant = InTouchRoomParticipant.builder().build();
        List<InTouchRoomLiveKey> sourceKeys = List.of(
                sourceKey("A", 0, 0),
                sourceKey("B", 0, 1),
                sourceKey("C", 0, 2));

        Method create = InTouchRoomStartService.class.getDeclaredMethod(
                "createGroupLiveKeys",
                InTouchRoom.class,
                InTouchRoomGroup.class,
                List.class,
                List.class);
        create.setAccessible(true);
        create.invoke(
                service,
                room,
                group,
                sourceKeys,
                List.of(firstParticipant, secondParticipant));

        assertThat(saved).hasSize(sourceKeys.size());
        assertThat(saved).allSatisfy(key -> {
            assertThat(key.getAssignmentState()).isEqualTo(LiveKeyAssignmentState.ASSIGNED);
            assertThat(key.getReleasedFromParticipant()).isNull();
            assertThat(key.getPooledAt()).isNull();
            assertThat(key.getStatus()).isEqualTo(expectedStatus);
        });
        assertThat(saved)
                .extracting(InTouchRoomGroupLiveKey::getAssignedParticipant)
                .containsExactly(firstParticipant, secondParticipant, firstParticipant);
        assertThat(saved)
                .extracting(InTouchRoomGroupLiveKey::getCurrentRow)
                .containsExactly(
                        expectCurrentPosition ? 0 : null,
                        expectCurrentPosition ? 0 : null,
                        expectCurrentPosition ? 0 : null);
        assertThat(saved)
                .extracting(InTouchRoomGroupLiveKey::getCurrentColumn)
                .containsExactly(
                        expectCurrentPosition ? 0 : null,
                        expectCurrentPosition ? 1 : null,
                        expectCurrentPosition ? 2 : null);
    }

    private InTouchRoomLiveKey sourceKey(String value, int row, int column) {
        return InTouchRoomLiveKey.builder()
                .keyValue(value)
                .keyType(LiveKeyType.CUSTOM)
                .targetRow(row)
                .targetColumn(column)
                .build();
    }
}
