ALTER TABLE tb_job_positions
    ADD COLUMN client_id BIGINT;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM tb_job_positions WHERE client_id IS NULL) THEN
        IF NOT EXISTS (SELECT 1 FROM tb_clients) THEN
            INSERT INTO tb_clients (name, cnpj, code)
            VALUES ('Cliente Padrão', '00000000000000', 'CLI-PADRAO');
        END IF;

        UPDATE tb_job_positions
        SET client_id = (SELECT id FROM tb_clients ORDER BY id LIMIT 1)
        WHERE client_id IS NULL;
    END IF;
END $$;

ALTER TABLE tb_job_positions
    ALTER COLUMN client_id SET NOT NULL;

ALTER TABLE tb_job_positions
    ADD CONSTRAINT fk_job_positions_client
        FOREIGN KEY (client_id) REFERENCES tb_clients (id);

CREATE INDEX idx_job_positions_client ON tb_job_positions (client_id);

ALTER TABLE tb_candidate_applications
    ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'TRIAGEM';

ALTER TABLE tb_candidate_applications
    ADD CONSTRAINT chk_candidate_applications_status
        CHECK (status IN (
            'TRIAGEM',
            'ENTRAR_EM_CONTATO',
            'ENTREVISTA_TECH_RECRUITER',
            'APROVADO_ENTREVISTA_CLIENTE',
            'APROVADO_CLIENTE',
            'REPROVADO',
            'DECLINOU',
            'STAND_BY'
        )) NOT VALID;

CREATE INDEX idx_candidate_applications_status ON tb_candidate_applications (status);

CREATE TABLE tb_candidate_application_comments (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    author_id BIGINT,
    author_email VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_application_comments_application
        FOREIGN KEY (application_id) REFERENCES tb_candidate_applications (id) ON DELETE CASCADE
);

CREATE INDEX idx_application_comments_application
    ON tb_candidate_application_comments (application_id);
CREATE INDEX idx_application_comments_created_at
    ON tb_candidate_application_comments (application_id, created_at DESC);
