package br.com.ats.consult.api.job.infrastructure.config;

import br.com.ats.consult.api.applicationform.domain.usecase.ManageApplicationFormUseCase;
import br.com.ats.consult.api.client.domain.port.out.ClientGateway;
import br.com.ats.consult.api.job.domain.port.out.JobPositionGateway;
import br.com.ats.consult.api.job.domain.usecase.ChangeJobPositionStatusUseCase;
import br.com.ats.consult.api.job.domain.usecase.CreateJobPositionUseCase;
import br.com.ats.consult.api.job.domain.usecase.DeleteJobPositionUseCase;
import br.com.ats.consult.api.job.domain.usecase.FindJobPositionUseCase;
import br.com.ats.consult.api.job.domain.usecase.UpdateJobPositionUseCase;
import br.com.ats.consult.api.job.domain.usecase.impl.ChangeJobPositionStatusUseCaseImpl;
import br.com.ats.consult.api.job.domain.usecase.impl.CreateJobPositionUseCaseImpl;
import br.com.ats.consult.api.job.domain.usecase.impl.DeleteJobPositionUseCaseImpl;
import br.com.ats.consult.api.job.domain.usecase.impl.FindJobPositionUseCaseImpl;
import br.com.ats.consult.api.job.domain.usecase.impl.UpdateJobPositionUseCaseImpl;
import br.com.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobPositionUseCaseConfiguration {

    @Bean
    public CreateJobPositionUseCase createJobPositionUseCase(JobPositionGateway gateway,
                                                             TechnologyGateway technologyGateway,
                                                             ClientGateway clientGateway,
                                                             ManageApplicationFormUseCase applicationFormUseCase) {
        return new CreateJobPositionUseCaseImpl(gateway, technologyGateway, clientGateway,
                applicationFormUseCase);
    }

    @Bean
    public FindJobPositionUseCase findJobPositionUseCase(JobPositionGateway gateway) {
        return new FindJobPositionUseCaseImpl(gateway);
    }

    @Bean
    public DeleteJobPositionUseCase deleteJobPositionUseCase(JobPositionGateway jobPositionGateway){
        return new DeleteJobPositionUseCaseImpl(jobPositionGateway);
    }

    @Bean
    public UpdateJobPositionUseCase updateJobPositionUseCase(JobPositionGateway jobPositionGateway,
                                                             TechnologyGateway technologyGateway,
                                                             ClientGateway clientGateway) {
        return new UpdateJobPositionUseCaseImpl(jobPositionGateway, technologyGateway, clientGateway);
    }

    @Bean
    public ChangeJobPositionStatusUseCase changeJobPositionStatusUseCase(JobPositionGateway jobPositionGateway) {
        return new ChangeJobPositionStatusUseCaseImpl(jobPositionGateway);
    }
}