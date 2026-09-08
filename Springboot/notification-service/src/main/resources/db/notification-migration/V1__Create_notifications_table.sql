-- Notification Service — Initial Schema
-- Managed by Flyway (table: flyway_schema_history_notification)

CREATE TABLE IF NOT EXISTS notifications (
    id                BIGSERIAL     PRIMARY KEY,
    recipient_user_id BIGINT        NOT NULL,
    type              VARCHAR(50)   NOT NULL,
    title             VARCHAR(255)  NOT NULL,
    message           TEXT,
    reference_id      BIGINT,
    is_read           BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Queries always filter by recipient — this index is critical for performance
CREATE INDEX IF NOT EXISTS idx_notif_recipient ON notifications (recipient_user_id);
-- UI shows newest-first; covering this with a DESC index avoids a sort
CREATE INDEX IF NOT EXISTS idx_notif_created   ON notifications (created_at DESC);
-- Unread badge query: filter by recipient + unread
CREATE INDEX IF NOT EXISTS idx_notif_unread    ON notifications (recipient_user_id, is_read)
    WHERE is_read = FALSE;
