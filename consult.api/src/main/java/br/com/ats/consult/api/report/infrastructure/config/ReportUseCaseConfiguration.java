package br.com.ats.consult.api.report.infrastructure.config;

import br.com.ats.consult.api.report.domain.port.out.ProcessDashboardGateway;
import br.com.ats.consult.api.report.domain.port.out.ProcessReportGateway;
import br.com.ats.consult.api.report.domain.usecase.GenerateProcessDashboardUseCase;
import br.com.ats.consult.api.report.domain.usecase.GenerateProcessReportUseCase;
import br.com.ats.consult.api.report.domain.usecase.impl.GenerateProcessDashboardUseCaseImpl;
import br.com.ats.consult.api.report.domain.usecase.impl.GenerateProcessReportUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportUseCaseConfiguration {

    @Bean
    public GenerateProcessReportUseCase generateProcessReportUseCase(ProcessReportGateway gateway) {
        return new GenerateProcessReportUseCaseImpl(gateway);
    }

    @Bean
    public GenerateProcessDashboardUseCase generateProcessDashboardUseCase(ProcessDashboardGateway gateway) {
        return new GenerateProcessDashboardUseCaseImpl(gateway);
    }
}
