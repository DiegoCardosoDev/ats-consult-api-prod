package br.com.ats.consult.api.technology.domain.usecase;

import br.com.ats.consult.api.technology.domain.model.Technology;
import br.com.ats.consult.api.technology.domain.model.TechnologyCategoryEnum;

/**
 * Contrato (Interface) para o caso de uso de criação de tecnologia.
 */
public interface CreateTechnologyUseCase {
    Technology execute(String name, TechnologyCategoryEnum category, Long parentId);
}