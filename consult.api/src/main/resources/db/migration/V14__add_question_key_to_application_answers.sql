ALTER TABLE tb_candidate_application_answers
    ADD COLUMN question_key VARCHAR(100);

UPDATE tb_candidate_application_answers answer
SET question_key = question.question_key
FROM tb_application_form_questions question
WHERE question.id = answer.question_id
  AND answer.question_key IS NULL;
