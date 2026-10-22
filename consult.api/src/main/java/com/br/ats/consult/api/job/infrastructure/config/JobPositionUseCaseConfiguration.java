package com.br.ats.consult.api.job.infrastructure.config;

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
                                                             TechnologyGateway technologyGateway) {
        return new CreateJobPositionUseCaseImpl(gateway,technologyGateway);
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
    public UpdateJobPositionUseCase updateJobPositionUseCase (JobPositionGateway jobPositionGateway, TechnologyGateway technologyGateway){
        return new UpdateJobPositionUseCaseImpl(jobPositionGateway, technologyGateway);
    }
}