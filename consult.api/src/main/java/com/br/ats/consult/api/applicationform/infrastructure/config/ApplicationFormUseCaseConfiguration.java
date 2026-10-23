package com.br.ats.consult.api.applicationform.infrastructure.config;

import com.br.ats.consult.api.applicationform.domain.port.out.ApplicationFormGateway;
import com.br.ats.consult.api.applicationform.domain.usecase.ManageApplicationFormUseCase;
import com.br.ats.consult.api.applicationform.domain.usecase.impl.ManageApplicationFormUseCaseImpl;
import com.br.ats.consult.api.job.domain.port.out.JobPositionGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationFormUseCaseConfiguration {

    @Bean
    public ManageApplicationFormUseCase manageApplicationFormUseCase(ApplicationFormGateway formGateway,
                                                                     JobPositionGateway jobPositionGateway) {
        return new ManageApplicationFormUseCaseImpl(formGateway, jobPositionGateway);
    }
}
