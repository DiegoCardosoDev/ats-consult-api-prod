ALTER TABLE tb_job_positions
    ADD COLUMN status_reason VARCHAR(500);

ALTER TABLE tb_candidate_applications
    DROP CONSTRAINT IF EXISTS chk_candidate_applications_status;

ALTER TABLE tb_candidate_applications
    ADD CONSTRAINT chk_candidate_applications_status
        CHECK (status IN (
            'TRIAGEM',
            'ENTRAR_EM_CONTATO',
            'ENTREVISTA_TECH_RECRUITER',
            'APROVADO_ENTREVISTA_CLIENTE',
            'APROVADO_CLIENTE',
            'CONTRATADO',
            'REPROVADO',
            'DECLINOU',
            'STAND_BY'
        )) NOT VALID;
