CREATE TABLE tb_application_forms (
    id BIGSERIAL PRIMARY KEY,
    job_position_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_application_forms_job UNIQUE (job_position_id),
    CONSTRAINT fk_application_forms_job FOREIGN KEY (job_position_id)
        REFERENCES tb_job_positions (id) ON DELETE CASCADE
);

CREATE TABLE tb_application_form_questions (
    id BIGSERIAL PRIMARY KEY,
    form_id BIGINT NOT NULL,
    prompt VARCHAR(500) NOT NULL,
    question_type VARCHAR(30) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    question_order INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_application_form_questions_form FOREIGN KEY (form_id)
        REFERENCES tb_application_forms (id) ON DELETE CASCADE,
    CONSTRAINT chk_application_form_question_type
        CHECK (question_type IN ('TEXT', 'TEXTAREA', 'YES_NO', 'NUMBER', 'SINGLE_CHOICE', 'MULTIPLE_CHOICE'))
);

CREATE TABLE tb_application_form_question_options (
    question_id BIGINT NOT NULL,
    option_order INTEGER NOT NULL DEFAULT 0,
    option_value VARCHAR(255),
    CONSTRAINT fk_application_form_options_question FOREIGN KEY (question_id)
        REFERENCES tb_application_form_questions (id) ON DELETE CASCADE
);

CREATE TABLE tb_candidate_application_answers (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    question_id BIGINT,
    question_prompt VARCHAR(500) NOT NULL,
    answer TEXT,
    answer_order INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT fk_candidate_application_answers_application FOREIGN KEY (application_id)
        REFERENCES tb_candidate_applications (id) ON DELETE CASCADE
);

CREATE INDEX idx_application_forms_job ON tb_application_forms (job_position_id);
CREATE INDEX idx_application_form_questions_form ON tb_application_form_questions (form_id);
CREATE INDEX idx_candidate_application_answers_application
    ON tb_candidate_application_answers (application_id);
