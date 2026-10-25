ALTER TABLE tb_application_form_questions
    DROP CONSTRAINT IF EXISTS chk_application_form_questions_technology_category;

ALTER TABLE tb_application_form_questions
    ADD CONSTRAINT chk_application_form_questions_technology_category
        CHECK (technology_category IS NULL OR technology_category IN (
            'LANGUAGE', 'FRAMEWORK', 'DATABASE', 'DEVOPS_INFRA', 'CLOUD_PROVIDER', 'AI_LLM',
            'TESTING_TOOL', 'ARCHITECTURE_STYLE', 'STORAGE', 'SERVERLESS', 'VERSION_CONTROL', 'OTHER'
        ));
