package com.br.ats.consult.api.job.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.job.domain.enums.ContractTypeEnum;
import com.br.ats.consult.api.job.domain.enums.EducationLevelEnum;
import com.br.ats.consult.api.job.domain.enums.EnglishLevelEnum;
import com.br.ats.consult.api.job.domain.enums.JobStatusEnum;
import com.br.ats.consult.api.job.domain.enums.WorkModelEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record JobPositionRequest(
        @NotNull(message = "O cliente da vaga é obrigatório")
        @Positive(message = "O cliente deve ter um identificador válido")
        Long clientId,
        @NotBlank(message = "O título da vaga é obrigatório")
        @Size(max = 255, message = "O título não pode exceder 255 caracteres")
        String title,
        @Size(max = 10000, message = "A descrição não pode exceder 10000 caracteres")
        String description,
        JobStatusEnum status,
        @NotNull(message = "O modelo de trabalho é obrigatório")
        WorkModelEnum workModel,
        @PositiveOrZero(message = "O salário mínimo não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "O salário mínimo deve ter no máximo 8 dígitos inteiros e 2 decimais")
        BigDecimal minSalary,
        @PositiveOrZero(message = "O salário máximo não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "O salário máximo deve ter no máximo 8 dígitos inteiros e 2 decimais")
        BigDecimal maxSalary,
        @Valid
        LocationRequest location,
        @NotNull(message = "O tipo de contrato é obrigatório")
        ContractTypeEnum contractType,
        EducationLevelEnum educationLevel,
        EnglishLevelEnum englishLevel,
        @Size(max = 100, message = "A vaga não pode ter mais de 100 tecnologias")
        List<@Positive(message = "O identificador da tecnologia deve ser positivo") Long> technologyIds
) {
}
