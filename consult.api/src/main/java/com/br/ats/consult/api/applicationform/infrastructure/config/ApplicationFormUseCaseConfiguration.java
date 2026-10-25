package com.br.ats.consult.api.applicationform.infrastructure.config;

import com.br.ats.consult.api.applicationform.domain.port.out.ApplicationFormGateway;
import com.br.ats.consult.api.applicationform.domain.usecase.ManageApplicationFormUseCase;
import com.br.ats.consult.api.applicationform.domain.usecase.impl.ManageApplicationFormUseCaseImpl;
import com.br.ats.consult.api.job.domain.port.out.JobPositionGateway;
import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationFormUseCaseConfiguration {

    @Bean
    public ManageApplicationFormUseCase manageApplicationFormUseCase(ApplicationFormGateway formGateway,
                                                                     JobPositionGateway jobPositionGateway,
                                                                     TechnologyGateway technologyGateway) {
        return new ManageApplicationFormUseCaseImpl(formGateway, jobPositionGateway, technologyGateway);
    }
}
