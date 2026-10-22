CREATE TABLE tb_job_positions (
                                  id BIGSERIAL PRIMARY KEY,
                                  title VARCHAR(255) NOT NULL,
                                  description TEXT,
                                  status VARCHAR(50) NOT NULL,
                                  work_model VARCHAR(50) NOT NULL,
                                  min_salary NUMERIC(10, 2),
                                  max_salary NUMERIC(10, 2),
                                  location_city VARCHAR(100),
                                  location_state VARCHAR(50),
                                  location_country VARCHAR(100)
);

CREATE TABLE tb_job_position_technologies (
                                              job_position_id BIGINT NOT NULL,
                                              technology_id BIGINT NOT NULL,
                                              CONSTRAINT fk_job_position FOREIGN KEY (job_position_id) REFERENCES tb_job_positions(id) ON DELETE CASCADE,
                                              CONSTRAINT fk_technology FOREIGN KEY (technology_id) REFERENCES tb_technologies(id) ON DELETE CASCADE,
                                              PRIMARY KEY (job_position_id, technology_id)
);