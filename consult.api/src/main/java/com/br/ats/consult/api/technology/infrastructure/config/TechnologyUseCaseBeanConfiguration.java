package com.br.ats.consult.api.technology.infrastructure.config;

import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import com.br.ats.consult.api.technology.domain.usecase.CreateTechnologyUseCase;
import com.br.ats.consult.api.technology.domain.usecase.FindTechnologyUseCase;
import com.br.ats.consult.api.technology.domain.usecase.impl.CreateTechnologyUseCaseImpl;
import com.br.ats.consult.api.technology.domain.usecase.impl.FindTechnologyUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Classe de configuração do Spring responsável por instanciar e gerenciar os beans
 * do módulo de tecnologias, mantendo o desacoplamento arquitetural limpo.
 */
@Configuration
public class TechnologyUseCaseBeanConfiguration {

    @Bean
    public CreateTechnologyUseCase createTechnologyUseCase(TechnologyGateway technologyGateway) {
        return new CreateTechnologyUseCaseImpl(technologyGateway);
    }

    @Bean
    public FindTechnologyUseCase findTechnologyUseCase(TechnologyGateway technologyGateway) {
        return new FindTechnologyUseCaseImpl(technologyGateway);
    }
}