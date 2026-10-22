package com.br.ats.consult.api.technology.infrastructure.persistence.repository;

import com.br.ats.consult.api.technology.infrastructure.persistence.entity.TechnologyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório do Spring Data JPA responsável pelas operações de CRUD
 * na tabela correspondente às entidades de tecnologia.
 */
@Repository
public interface SpringDataTechnologyRepository extends JpaRepository<TechnologyEntity, Long> {

    List<TechnologyEntity> findByParentId(Long parentId);

    @Query("SELECT t FROM TechnologyEntity t WHERE t.id IN :ids")
    List<TechnologyEntity> findAllByIdIn(@Param("ids") List<Long> ids);

    boolean existsByNameIgnoreCaseAndParentIdIsNull(String name);

    boolean existsByNameIgnoreCaseAndParentId(String name, Long parentId);
}