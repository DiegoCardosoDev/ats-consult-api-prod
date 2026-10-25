package com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.candidate.domain.model.Candidate;
import com.br.ats.consult.api.job.domain.enums.EducationLevelEnum;
import com.br.ats.consult.api.job.domain.enums.EnglishLevelEnum;

import java.util.List;

public record CandidateResponse(
        Long id,
        String name,
        Integer age,
        String role,
        String linkedinUrl,
        String email,
        String phone,
        EducationLevelEnum educationLevel,
        EnglishLevelEnum englishLevel,
        List<CandidateTechnologyResponse> technologies,
        CandidateAddressResponse address
) {

    public static CandidateResponse fromDomain(Candidate domain) {
        if (domain == null) {
            return null;
        }
        return new CandidateResponse(
                domain.getId(),
                domain.getName(),
                domain.getAge(),
                domain.getRole(),
                domain.getLinkedinUrl(),
                domain.getEmail(),
                domain.getPhone(),
                domain.getEducationLevel(),
                domain.getEnglishLevel(),
                domain.getTechnologies() == null
                        ? List.of()
                        : domain.getTechnologies().stream()
                        .map(CandidateTechnologyResponse::fromDomain)
                        .toList(),
                CandidateAddressResponse.fromDomain(domain.getAddress())
        );
    }
}
