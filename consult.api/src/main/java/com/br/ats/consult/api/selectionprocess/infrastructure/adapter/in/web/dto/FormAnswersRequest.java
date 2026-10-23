package com.br.ats.consult.api.selectionprocess.infrastructure.adapter.in.web.dto;

import jakarta.validation.Valid;

import java.util.List;

public record FormAnswersRequest(
        List<@Valid FormAnswerRequest> answers
) {
}
