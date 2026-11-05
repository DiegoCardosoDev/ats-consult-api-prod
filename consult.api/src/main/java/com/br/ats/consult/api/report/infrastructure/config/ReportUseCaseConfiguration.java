package com.br.ats.consult.api.report.infrastructure.config;

import com.br.ats.consult.api.report.domain.port.out.ProcessDashboardGateway;
import com.br.ats.consult.api.report.domain.port.out.ProcessReportGateway;
import com.br.ats.consult.api.report.domain.usecase.GenerateProcessDashboardUseCase;
import com.br.ats.consult.api.report.domain.usecase.GenerateProcessReportUseCase;
import com.br.ats.consult.api.report.domain.usecase.impl.GenerateProcessDashboardUseCaseImpl;
import com.br.ats.consult.api.report.domain.usecase.impl.GenerateProcessReportUseCaseImpl;
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
