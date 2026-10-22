package com.br.ats.consult.api.technology.domain.usecase;

import com.br.ats.consult.api.technology.domain.model.Technology;
import com.br.ats.consult.api.technology.domain.model.TechnologyCategoryEnum;

/**
 * Contrato (Interface) para o caso de uso de criação de tecnologia.
 */
public interface CreateTechnologyUseCase {
    Technology execute(String name, TechnologyCategoryEnum category, Long parentId);
}