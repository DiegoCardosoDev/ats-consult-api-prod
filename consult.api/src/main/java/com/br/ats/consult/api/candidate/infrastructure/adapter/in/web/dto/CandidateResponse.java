package com.br.ats.consult.api.candidate.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.candidate.domain.model.Candidate;

public record CandidateResponse(
        Long id,
        String name,
        Integer age,
        String role,
        String linkedinUrl,
        String email,
        String phone,
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
                CandidateAddressResponse.fromDomain(domain.getAddress())
        );
    }
}
