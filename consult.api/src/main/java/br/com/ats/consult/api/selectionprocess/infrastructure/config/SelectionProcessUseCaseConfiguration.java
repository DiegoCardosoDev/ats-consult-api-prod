package br.com.ats.consult.api.selectionprocess.infrastructure.config;

import br.com.ats.consult.api.applicationform.domain.usecase.ManageApplicationFormUseCase;
import br.com.ats.consult.api.candidate.domain.port.out.CandidateGateway;
import br.com.ats.consult.api.client.domain.port.out.ClientGateway;
import br.com.ats.consult.api.job.domain.port.out.JobPositionGateway;
import br.com.ats.consult.api.selectionprocess.domain.port.out.ResumeStorage;
import br.com.ats.consult.api.selectionprocess.domain.port.out.SelectionProcessGateway;
import br.com.ats.consult.api.selectionprocess.domain.usecase.ApplyCandidateUseCase;
import br.com.ats.consult.api.selectionprocess.domain.usecase.CreateSelectionProcessUseCase;
import br.com.ats.consult.api.selectionprocess.domain.usecase.ManageApplicationCommentUseCase;
import br.com.ats.consult.api.selectionprocess.domain.usecase.UpdateApplicationStatusUseCase;
import br.com.ats.consult.api.selectionprocess.domain.usecase.FindSelectionProcessUseCase;
import br.com.ats.consult.api.selectionprocess.domain.usecase.impl.ApplyCandidateUseCaseImpl;
import br.com.ats.consult.api.selectionprocess.domain.usecase.impl.CreateSelectionProcessUseCaseImpl;
import br.com.ats.consult.api.selectionprocess.domain.usecase.impl.FindSelectionProcessUseCaseImpl;
import br.com.ats.consult.api.selectionprocess.domain.usecase.impl.ManageApplicationCommentUseCaseImpl;
import br.com.ats.consult.api.selectionprocess.domain.usecase.impl.UpdateApplicationStatusUseCaseImpl;
import br.com.ats.consult.api.technology.domain.port.out.TechnologyGateway;
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
    public UpdateApplicationStatusUseCase updateApplicationStatusUseCase(SelectionProcessGateway gateway) {
        return new UpdateApplicationStatusUseCaseImpl(gateway);
    }

    @Bean
    public ManageApplicationCommentUseCase manageApplicationCommentUseCase(SelectionProcessGateway gateway) {
        return new ManageApplicationCommentUseCaseImpl(gateway);
    }

    @Bean
    public ApplyCandidateUseCase applyCandidateUseCase(SelectionProcessGateway processGateway,
                                                       CandidateGateway candidateGateway,
                                                       JobPositionGateway jobPositionGateway,
                                                       ManageApplicationFormUseCase applicationFormUseCase,
                                                       ResumeStorage resumeStorage,
                                                       TechnologyGateway technologyGateway) {
        return new ApplyCandidateUseCaseImpl(processGateway, candidateGateway,
                jobPositionGateway, applicationFormUseCase, resumeStorage, technologyGateway);
    }
}
