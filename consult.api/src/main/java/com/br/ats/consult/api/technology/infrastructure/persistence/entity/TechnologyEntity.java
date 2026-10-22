package com.br.ats.consult.api.technology.infrastructure.persistence.entity;

import jakarta.persistence.*;

/**
 * Entidade JPA que representa a tabela de tecnologias no banco de dados relacional (PostgreSQL).
 * Mapeada especificamente para persistência de dados.
 */
@Entity
@Table(name = "tb_technologies")
public class TechnologyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "parent_id")
    private Long parentId;

    public TechnologyEntity() {}

    /**
     * Construtor com argumentos para inicialização da entidade JPA.
     */
    public TechnologyEntity(Long id, String name, String category, Long parentId) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.parentId = parentId;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
}