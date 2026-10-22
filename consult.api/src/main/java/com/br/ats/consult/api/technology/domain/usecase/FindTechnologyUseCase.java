package com.br.ats.consult.api.technology.domain.usecase;

import com.br.ats.consult.api.technology.domain.model.Technology;

import java.util.List;
import java.util.Optional;

/**
 * Interface que define as operações do caso de uso para consulta do catálogo de tecnologias.
 */
public interface FindTechnologyUseCase {

    /**
     * Retorna a lista completa de tecnologias cadastradas.
     *
     * @return Lista de tecnologias.
     */
    List<Technology> findAll();

    /**
     * Busca uma tecnologia específica pelo seu identificador único.
     *
     * @param id Identificador único da tecnologia.
     * @return Opcional contendo a tecnologia se encontrada.
     */
    Technology findById(Long id);

    List<Technology> findByParentId(Long parentId); // Novo método
}