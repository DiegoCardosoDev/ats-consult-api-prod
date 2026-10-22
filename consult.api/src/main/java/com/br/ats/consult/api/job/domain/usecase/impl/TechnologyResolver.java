package com.br.ats.consult.api.job.domain.usecase.impl;

import com.br.ats.consult.api.technology.domain.exception.TechnologyNotFoundException;
import com.br.ats.consult.api.technology.domain.model.Technology;
import com.br.ats.consult.api.technology.domain.port.out.TechnologyGateway;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

final class TechnologyResolver {

    private TechnologyResolver() {
    }

    static List<Technology> resolve(TechnologyGateway gateway, List<Long> requestedIds) {
        if (requestedIds == null || requestedIds.isEmpty()) {
            return List.of();
        }

        if (requestedIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("A lista de tecnologias não pode conter IDs nulos.");
        }

        List<Long> uniqueIds = requestedIds.stream().distinct().toList();
        List<Technology> foundTechnologies = gateway.findAllByIds(uniqueIds);

        Set<Long> foundIds = foundTechnologies.stream()
                .map(Technology::getId)
                .collect(Collectors.toSet());

        List<Long> missingIds = uniqueIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        if (!missingIds.isEmpty()) {
            throw new TechnologyNotFoundException(missingIds);
        }

        return foundTechnologies;
    }
}
