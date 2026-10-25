ALTER TABLE tb_candidates
    ADD COLUMN education_level VARCHAR(50),
    ADD COLUMN english_level VARCHAR(50);

ALTER TABLE tb_candidates
    ADD CONSTRAINT chk_candidates_education_level
        CHECK (education_level IS NULL OR education_level IN (
            'HIGH_SCHOOL', 'ASSOCIATE', 'BACHELORS', 'SPECIALIZATION', 'MASTERS', 'DOCTORATE'
        )),
    ADD CONSTRAINT chk_candidates_english_level
        CHECK (english_level IS NULL OR english_level IN (
            'NONE', 'BASIC', 'INTERMEDIATE', 'ADVANCED', 'FLUENT'
        ));

CREATE TABLE tb_candidate_technologies (
    candidate_id BIGINT NOT NULL,
    technology_id BIGINT NOT NULL,
    CONSTRAINT pk_candidate_technologies PRIMARY KEY (candidate_id, technology_id),
    CONSTRAINT fk_candidate_technologies_candidate FOREIGN KEY (candidate_id)
        REFERENCES tb_candidates (id) ON DELETE CASCADE,
    CONSTRAINT fk_candidate_technologies_technology FOREIGN KEY (technology_id)
        REFERENCES tb_technologies (id) ON DELETE CASCADE
);

CREATE INDEX idx_candidate_technologies_technology
    ON tb_candidate_technologies (technology_id);

ALTER TABLE tb_application_form_questions
    ADD COLUMN question_key VARCHAR(100),
    ADD COLUMN technology_category VARCHAR(50),
    ADD COLUMN parent_technology_id BIGINT,
    ADD COLUMN max_selections INTEGER,
    ADD COLUMN system_generated BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE tb_application_form_questions
    ADD CONSTRAINT fk_application_form_questions_parent_technology
        FOREIGN KEY (parent_technology_id) REFERENCES tb_technologies (id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_application_form_questions_technology_category
        CHECK (technology_category IS NULL OR technology_category IN (
            'LANGUAGE', 'FRAMEWORK', 'DATABASE', 'DEVOPS_INFRA', 'CLOUD_PROVIDER', 'AI_LLM',
            'TESTING_TOOL', 'ARCHITECTURE_STYLE', 'STORAGE', 'SERVERLESS', 'OTHER'
        )),
    ADD CONSTRAINT chk_application_form_questions_max_selections
        CHECK (max_selections IS NULL OR max_selections > 0);

CREATE INDEX idx_application_form_questions_parent_technology
    ON tb_application_form_questions (parent_technology_id);
