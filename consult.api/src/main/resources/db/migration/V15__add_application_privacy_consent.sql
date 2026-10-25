ALTER TABLE tb_candidate_applications
    ADD COLUMN consent_accepted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN consent_accepted_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN consent_version VARCHAR(50),
    ADD COLUMN consent_ip VARCHAR(45);
