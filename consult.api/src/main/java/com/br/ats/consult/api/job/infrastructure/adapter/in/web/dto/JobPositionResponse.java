package com.br.ats.consult.api.job.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.job.domain.enums.*;
import com.br.ats.consult.api.job.domain.model.JobPosition;

import java.math.BigDecimal;
import java.util.List;

public record JobPositionResponse(
        Long id,
        JobClientResponse client,
        String title,
        String description,
        JobStatusEnum status,
        WorkModelEnum workModel,
        BigDecimal minSalary,
        BigDecimal maxSalary,
        LocationResponse location,
        ContractTypeEnum contractType,
        EducationLevelEnum educationLevel,
        EnglishLevelEnum englishLevel,
        List<JobTechnologyResponse> technologies // 👈 Usa o DTO local do módulo job em vez do TechnologyResponse externo
) {
    public static JobPositionResponse fromDomain(JobPosition domain) {
        if (domain == null) return null;

        List<JobTechnologyResponse> techResponses = null;
        if (domain.getTechnologies() != null) {
            techResponses = domain.getTechnologies().stream()
                    .map(JobTechnologyResponse::fromDomain) // 👈 Delega a conversão para o método estático do DTO local
                    .toList();
        }

        return new JobPositionResponse(
                domain.getId(),
                JobClientResponse.fromDomain(domain.getClient()),
                domain.getTitle(),
                domain.getDescription(),
                domain.getStatus(),
                domain.getWorkModel(),
                domain.getMinSalary(),
                domain.getMaxSalary(),
                LocationResponse.fromDomain(domain.getLocation()),
                domain.getContractType(),
                domain.getEducationLevel(),
                domain.getEnglishLevel(),
                techResponses
        );
    }
}