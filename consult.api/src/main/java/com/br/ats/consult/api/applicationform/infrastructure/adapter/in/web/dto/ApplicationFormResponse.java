package com.br.ats.consult.api.applicationform.infrastructure.adapter.in.web.dto;

import com.br.ats.consult.api.applicationform.domain.model.ApplicationForm;

import java.util.List;

public record ApplicationFormResponse(
        Long id,
        Long jobPositionId,
        String title,
        String description,
        boolean active,
        List<ApplicationFormQuestionResponse> questions
) {

    public static ApplicationFormResponse fromDomain(ApplicationForm form) {
        if (form == null) {
            return null;
        }
        return new ApplicationFormResponse(
                form.getId(),
                form.getJobPositionId(),
                form.getTitle(),
                form.getDescription(),
                form.isActive(),
                form.getQuestions() == null
                        ? List.of()
                        : form.getQuestions().stream()
                        .map(ApplicationFormQuestionResponse::fromDomain)
                        .toList()
        );
    }
}
