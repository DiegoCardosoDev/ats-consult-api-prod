package br.com.ats.consult.api.selectionprocess.domain.model;

public record CandidateApplicationResult(
        SelectionProcess selectionProcess,
        CandidateApplication application
) {
}
