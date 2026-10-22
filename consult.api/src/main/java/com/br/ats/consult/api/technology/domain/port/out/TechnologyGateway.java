package com.br.ats.consult.api.technology.domain.port.out;

import com.br.ats.consult.api.technology.domain.model.Technology;
import java.util.List;
import java.util.Optional;

/**
 * Porta de saída (Output Port) que define o contrato de persistência para o domínio de tecnologias.
 * Segue o princípio da inversão de dependência da Clean Architecture, impedindo que o
 * domínio conheça detalhes de banco de dados.
 */
public interface TechnologyGateway {
    /**
     * Salva ou atualiza uma tecnologia no repositório.
     *
     * @param technology Instância da tecnologia de domínio.
     * @return A tecnologia salva atualizada.
     */
    Technology save(Technology technology);

    /**
     * Busca uma tecnologia pelo seu identificador único.
     *
     * @param id ID da tecnologia.
     * @return Optional contendo a tecnologia se encontrada.
     */
    Optional<Technology> findById(Long id);

    /**
     * Retorna a lista completa de tecnologias cadastradas.
     *
     * @return Lista de tecnologias.
     */
    List<Technology> findAll();

    /**
     * Verifica se existe uma tecnologia cadastrada com o ID informado.
     *
     * @param id ID a ser verificado.
     * @return true se existir, false caso contrário.
     */
    boolean existsById(Long id);

    List<Technology> findByParentId(Long parentId);

    List<Technology> findAllByIds(List<Long> ids);

    boolean existsByNameAndParent(String name, Long parentId);
}