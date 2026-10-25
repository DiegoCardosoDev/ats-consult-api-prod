package com.br.ats.consult.api.candidate.infrastructure.config;

import com.br.ats.consult.api.candidate.domain.port.out.CandidateGateway;
import com.br.ats.consult.api.candidate.domain.usecase.CreateCandidateUseCase;
import com.br.ats.consult.api.candidate.domain.usecase.FindCandidateUseCase;
import com.br.ats.consult.api.candidate.domain.usecase.UpdateCandidateUseCase;
import com.br.ats.consult.api.candidate.domain.usecase.impl.CreateCandidateUseCaseImpl;
import com.br.ats.consult.api.candidate.domain.usecase.impl.FindCandidateUseCaseImpl;
import com.br.ats.consult.api.candidate.domain.usecase.impl.UpdateCandidateUseCaseImpl;
import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CandidateUseCaseConfiguration {

    @Bean
    public CreateCandidateUseCase createCandidateUseCase(CandidateGateway gateway,
                                                         TechnologyGateway technologyGateway) {
        return new CreateCandidateUseCaseImpl(gateway, technologyGateway);
    }

    @Bean
    public FindCandidateUseCase findCandidateUseCase(CandidateGateway gateway) {
        return new FindCandidateUseCaseImpl(gateway);
    }

    @Bean
    public UpdateCandidateUseCase updateCandidateUseCase(CandidateGateway gateway,
                                                         TechnologyGateway technologyGateway) {
        return new UpdateCandidateUseCaseImpl(gateway, technologyGateway);
    }
}
