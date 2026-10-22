ALTER TABLE tb_users
    ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE tb_users
    ADD CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'TECH_RECRUITER', 'USER')) NOT VALID;

ALTER TABLE tb_job_positions
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

ALTER TABLE tb_job_positions
    ADD CONSTRAINT chk_job_positions_salary_range
        CHECK (min_salary IS NULL OR max_salary IS NULL OR min_salary <= max_salary) NOT VALID;

CREATE UNIQUE INDEX IF NOT EXISTS idx_technologies_name_parent_unique
    ON tb_technologies (LOWER(name), COALESCE(parent_id, 0));
