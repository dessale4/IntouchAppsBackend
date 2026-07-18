ALTER TABLE intouch_room_group_live_key_tbl
    ADD COLUMN assignment_state VARCHAR(30);

UPDATE intouch_room_group_live_key_tbl
SET assignment_state = 'ASSIGNED'
WHERE assignment_state IS NULL;

ALTER TABLE intouch_room_group_live_key_tbl
    ALTER COLUMN assignment_state SET NOT NULL,
    ADD COLUMN released_from_participant_id BIGINT,
    ADD COLUMN pooled_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE intouch_room_group_live_key_tbl
    ADD CONSTRAINT fk_group_live_key_released_from_participant
        FOREIGN KEY (released_from_participant_id)
        REFERENCES intouch_room_participant_tbl (id);

CREATE INDEX idx_group_live_key_pool_lookup
    ON intouch_room_group_live_key_tbl
        (room_id, group_id, assignment_state, status, assigned_order);
