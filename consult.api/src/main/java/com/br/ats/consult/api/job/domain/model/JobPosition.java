package com.br.ats.consult.api.job.domain.model;

import com.br.ats.consult.api.client.domain.model.Client;
import com.br.ats.consult.api.job.domain.enums.*;
import com.br.ats.consult.api.technology.domain.model.Technology;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPosition {
    private Long id;
    private Client client;
    private String title;
    private String description;
    private JobStatusEnum status;
    private WorkModelEnum workModel;  // REMOTE, HYBRID, ON_SITE
    private ContractTypeEnum contractType;
    private EducationLevelEnum educationLevel;
    private EnglishLevelEnum englishLevel;
    private BigDecimal minSalary;      // Salário Mínimo / De
    private BigDecimal maxSalary;      // Salário Máximo / Até
    private Location location;
    private List<Technology> technologies; // IDs das tecnologias exigidas para a vaga

    public void validate() {
        if (client == null || client.getId() == null) {
            throw new IllegalArgumentException("O cliente da vaga é obrigatório.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("O título da vaga é obrigatório.");
        }
        if (status == null) {
            throw new IllegalArgumentException("O estado da vaga é obrigatório.");
        }
        if (workModel == null) {
            throw new IllegalArgumentException("O modelo de trabalho é obrigatório.");
        }
        if (contractType == null) {
            throw new IllegalArgumentException("O tipo de contrato é obrigatório.");
        }
        if (minSalary != null && minSalary.signum() < 0) {
            throw new IllegalArgumentException("O salário mínimo não pode ser negativo.");
        }
        if (maxSalary != null && maxSalary.signum() < 0) {
            throw new IllegalArgumentException("O salário máximo não pode ser negativo.");
        }
        if (minSalary != null && maxSalary != null && minSalary.compareTo(maxSalary) > 0) {
            throw new IllegalArgumentException("O salário mínimo não pode ser superior ao salário máximo.");
        }
    }
}