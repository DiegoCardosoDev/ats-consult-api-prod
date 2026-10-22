CREATE TABLE tb_clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    cnpj VARCHAR(14) NOT NULL,
    code VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_clients_cnpj UNIQUE (cnpj),
    CONSTRAINT uq_clients_code UNIQUE (code)
);

CREATE TABLE tb_candidates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    age INTEGER NOT NULL,
    role VARCHAR(150) NOT NULL,
    linkedin_url VARCHAR(500),
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    address_street VARCHAR(150),
    address_number VARCHAR(20),
    address_complement VARCHAR(100),
    address_neighborhood VARCHAR(100),
    address_city VARCHAR(100),
    address_state VARCHAR(50),
    address_zip_code VARCHAR(20),
    address_country VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_candidates_age CHECK (age BETWEEN 16 AND 120)
);

CREATE UNIQUE INDEX uq_candidates_email_lower ON tb_candidates (LOWER(email));
CREATE INDEX idx_candidates_name_lower ON tb_candidates (LOWER(name));
CREATE INDEX idx_candidates_role_lower ON tb_candidates (LOWER(role));

CREATE TABLE tb_selection_processes (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    job_position_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    opened_at DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_selection_process_client FOREIGN KEY (client_id) REFERENCES tb_clients (id),
    CONSTRAINT fk_selection_process_job_position FOREIGN KEY (job_position_id) REFERENCES tb_job_positions (id),
    CONSTRAINT chk_selection_process_status
        CHECK (status IN ('OPEN', 'PAUSED', 'CLOSED', 'CANCELLED'))
);

CREATE UNIQUE INDEX uq_selection_process_open
    ON tb_selection_processes (client_id, job_position_id)
    WHERE status = 'OPEN';

CREATE INDEX idx_selection_process_client ON tb_selection_processes (client_id);
CREATE INDEX idx_selection_process_job_position ON tb_selection_processes (job_position_id);

CREATE TABLE tb_candidate_applications (
    id BIGSERIAL PRIMARY KEY,
    selection_process_id BIGINT NOT NULL,
    candidate_id BIGINT NOT NULL,
    resume_filename VARCHAR(255) NOT NULL,
    resume_content_type VARCHAR(100) NOT NULL,
    resume_size_bytes BIGINT NOT NULL,
    resume_storage_key VARCHAR(255) NOT NULL,
    applied_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_application_selection_process FOREIGN KEY (selection_process_id)
        REFERENCES tb_selection_processes (id) ON DELETE CASCADE,
    CONSTRAINT fk_application_candidate FOREIGN KEY (candidate_id)
        REFERENCES tb_candidates (id) ON DELETE CASCADE,
    CONSTRAINT uq_application_process_candidate UNIQUE (selection_process_id, candidate_id)
);

CREATE INDEX idx_candidate_applications_process ON tb_candidate_applications (selection_process_id);
CREATE INDEX idx_candidate_applications_candidate ON tb_candidate_applications (candidate_id);
