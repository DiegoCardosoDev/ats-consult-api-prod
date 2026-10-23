package com.br.ats.consult.api.selectionprocess.infrastructure.config;

import com.br.ats.consult.api.applicationform.domain.port.out.ApplicationFormGateway;
import com.br.ats.consult.api.candidate.domain.port.out.CandidateGateway;
import com.br.ats.consult.api.client.domain.port.out.ClientGateway;
import com.br.ats.consult.api.job.domain.port.out.JobPositionGateway;
import com.br.ats.consult.api.selectionprocess.domain.port.out.ResumeStorage;
import com.br.ats.consult.api.selectionprocess.domain.port.out.SelectionProcessGateway;
import com.br.ats.consult.api.selectionprocess.domain.usecase.ApplyCandidateUseCase;
import com.br.ats.consult.api.selectionprocess.domain.usecase.CreateSelectionProcessUseCase;
import com.br.ats.consult.api.selectionprocess.domain.usecase.FindSelectionProcessUseCase;
import com.br.ats.consult.api.selectionprocess.domain.usecase.impl.ApplyCandidateUseCaseImpl;
import com.br.ats.consult.api.selectionprocess.domain.usecase.impl.CreateSelectionProcessUseCaseImpl;
import com.br.ats.consult.api.selectionprocess.domain.usecase.impl.FindSelectionProcessUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SelectionProcessUseCaseConfiguration {

    @Bean
    public CreateSelectionProcessUseCase createSelectionProcessUseCase(SelectionProcessGateway gateway,
                                                                       ClientGateway clientGateway,
                                                                       JobPositionGateway jobPositionGateway) {
        return new CreateSelectionProcessUseCaseImpl(gateway, clientGateway, jobPositionGateway);
    }

    @Bean
    public FindSelectionProcessUseCase findSelectionProcessUseCase(SelectionProcessGateway gateway) {
        return new FindSelectionProcessUseCaseImpl(gateway);
    }

    @Bean
    public ApplyCandidateUseCase applyCandidateUseCase(SelectionProcessGateway processGateway,
                                                       CandidateGateway candidateGateway,
                                                       ClientGateway clientGateway,
                                                       JobPositionGateway jobPositionGateway,
                                                       ApplicationFormGateway applicationFormGateway,
                                                       ResumeStorage resumeStorage) {
        return new ApplyCandidateUseCaseImpl(processGateway, candidateGateway, clientGateway,
                jobPositionGateway, applicationFormGateway, resumeStorage);
    }
}
