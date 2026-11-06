package br.com.ats.consult.api.applicationform.infrastructure.config;

import br.com.ats.consult.api.applicationform.domain.port.out.ApplicationFormGateway;
import br.com.ats.consult.api.applicationform.domain.usecase.ManageApplicationFormUseCase;
import br.com.ats.consult.api.applicationform.domain.usecase.impl.ManageApplicationFormUseCaseImpl;
import br.com.ats.consult.api.job.domain.port.out.JobPositionGateway;
import br.com.ats.consult.api.technology.domain.port.out.TechnologyGateway;
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
