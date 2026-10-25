package com.br.ats.consult.api.candidate.domain.usecase.impl;

import com.br.ats.consult.api.technology.domain.exception.TechnologyNotFoundException;
import com.br.ats.consult.api.technology.domain.model.Technology;
import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

final class CandidateTechnologyResolver {

    private CandidateTechnologyResolver() {
    }

    static List<Technology> resolve(TechnologyGateway gateway, List<Technology> technologies) {
        if (technologies == null || technologies.isEmpty()) {
            return List.of();
        }

        List<Long> requestedIds = technologies.stream()
                .map(Technology::getId)
                .toList();

        if (requestedIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("A lista de tecnologias não pode conter IDs nulos.");
        }

        List<Long> uniqueIds = requestedIds.stream().distinct().toList();
        List<Technology> found = gateway.findAllByIds(uniqueIds);
        Set<Long> foundIds = found.stream()
                .map(Technology::getId)
                .collect(Collectors.toSet());

        List<Long> missingIds = uniqueIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();
        if (!missingIds.isEmpty()) {
            throw new TechnologyNotFoundException(missingIds);
        }

        return found;
    }
}
