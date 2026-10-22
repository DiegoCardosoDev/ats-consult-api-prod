package com.br.ats.consult.api.technology.domain.model;

import lombok.*;

/**
 * Entidade de domínio puro que representa uma Tecnologia no sistema ATS.
 * Contém as regras de negócio essenciais e validações, mantendo-se totalmente
 * isolada de frameworks externos e detalhes de persistência.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Technology {
    private Long id;
    private String name;
    private TechnologyCategoryEnum category;
    private Long parentId; // Suporte à hierarquia de tecnologias (ex: sub-tecnologias)

}