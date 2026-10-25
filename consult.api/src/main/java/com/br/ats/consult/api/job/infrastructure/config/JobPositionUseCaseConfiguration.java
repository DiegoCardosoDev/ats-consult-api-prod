package com.br.ats.consult.api.job.infrastructure.config;

import com.br.ats.consult.api.client.domain.port.out.ClientGateway;
import com.br.ats.consult.api.job.domain.port.out.JobPositionGateway;
import com.br.ats.consult.api.job.domain.usecase.CreateJobPositionUseCase;
import com.br.ats.consult.api.job.domain.usecase.DeleteJobPositionUseCase;
import com.br.ats.consult.api.job.domain.usecase.FindJobPositionUseCase;
import com.br.ats.consult.api.job.domain.usecase.UpdateJobPositionUseCase;
import com.br.ats.consult.api.job.domain.usecase.impl.CreateJobPositionUseCaseImpl;
import com.br.ats.consult.api.job.domain.usecase.impl.DeleteJobPositionUseCaseImpl;
import com.br.ats.consult.api.job.domain.usecase.impl.FindJobPositionUseCaseImpl;
import com.br.ats.consult.api.job.domain.usecase.impl.UpdateJobPositionUseCaseImpl;
import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobPositionUseCaseConfiguration {

    @Bean
    public CreateJobPositionUseCase createJobPositionUseCase(JobPositionGateway gateway,
                                                             TechnologyGateway technologyGateway,
                                                             ClientGateway clientGateway) {
        return new CreateJobPositionUseCaseImpl(gateway, technologyGateway, clientGateway);
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
}