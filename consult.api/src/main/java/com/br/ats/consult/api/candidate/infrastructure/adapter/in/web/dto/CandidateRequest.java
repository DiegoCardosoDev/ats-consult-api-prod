package com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.job.domain.enums.EducationLevelEnum;
import com.br.ats.consult.api.job.domain.enums.EnglishLevelEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CandidateRequest(
        @NotBlank(message = "O nome do candidato é obrigatório")
        @Size(max = 150, message = "O nome do candidato não pode exceder 150 caracteres")
        String name,
        @NotNull(message = "A idade do candidato é obrigatória")
        @Min(value = 16, message = "A idade do candidato deve ser pelo menos 16")
        @Max(value = 120, message = "A idade do candidato deve ser no máximo 120")
        Integer age,
        @NotBlank(message = "O cargo do candidato é obrigatório")
        @Size(max = 150, message = "O cargo do candidato não pode exceder 150 caracteres")
        String role,
        @Size(max = 500, message = "O LinkedIn não pode exceder 500 caracteres")
        @Pattern(regexp = "^$|^https?://.+", message = "O LinkedIn deve ser um URL válido")
        String linkedinUrl,
        @NotBlank(message = "O e-mail do candidato é obrigatório")
        @Email(message = "O e-mail do candidato é inválido")
        @Size(max = 255, message = "O e-mail do candidato não pode exceder 255 caracteres")
        String email,
        @NotBlank(message = "O telefone do candidato é obrigatório")
        @Size(min = 8, max = 30, message = "O telefone deve ter entre 8 e 30 caracteres")
        @Pattern(regexp = "^[0-9+()\\-\\s]+$", message = "O telefone contém caracteres inválidos")
        String phone,
        EducationLevelEnum educationLevel,
        EnglishLevelEnum englishLevel,
        @Size(max = 100, message = "O candidato não pode ter mais de 100 tecnologias")
        List<@Min(value = 1, message = "O identificador da tecnologia deve ser positivo") Long> technologyIds,
        @Valid
        CandidateAddressRequest address
) {
}
