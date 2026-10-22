CREATE TABLE tb_technologies (
                                 id BIGSERIAL PRIMARY KEY,
                                 name VARCHAR(100) NOT NULL,
                                 category VARCHAR(50) NOT NULL,
                                 parent_id BIGINT,
                                 CONSTRAINT fk_technology_parent FOREIGN KEY (parent_id) REFERENCES tb_technologies (id)
);

-- Índice para otimizar buscas por categoria
CREATE INDEX idx_technologies_category ON tb_technologies (category);

-- Índice para otimizar buscas por tecnologias filhas na hierarquia
CREATE INDEX idx_technologies_parent_id ON tb_technologies (parent_id);