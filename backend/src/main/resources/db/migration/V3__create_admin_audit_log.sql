CREATE TABLE admin_audit_log (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    resource VARCHAR(100) NOT NULL,
    detail VARCHAR(500),
    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_admin_audit_occurred ON admin_audit_log(occurred_at DESC);
CREATE INDEX idx_admin_audit_username ON admin_audit_log(username, occurred_at DESC);
