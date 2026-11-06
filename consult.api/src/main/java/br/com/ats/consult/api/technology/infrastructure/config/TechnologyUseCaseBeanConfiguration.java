package br.com.ats.consult.api.technology.infrastructure.config;

import br.com.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import br.com.ats.consult.api.technology.domain.usecase.CreateTechnologyUseCase;
import br.com.ats.consult.api.technology.domain.usecase.FindTechnologyUseCase;
import br.com.ats.consult.api.technology.domain.usecase.impl.CreateTechnologyUseCaseImpl;
import br.com.ats.consult.api.technology.domain.usecase.impl.FindTechnologyUseCaseImpl;
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