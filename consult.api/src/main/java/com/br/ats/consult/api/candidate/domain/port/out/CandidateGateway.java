package com.br.ats.consult.api.candidate.domain.port.out;

import com.br.ats.consult.api.candidate.domain.model.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CandidateGateway {

    Candidate save(Candidate candidate);

    Optional<Candidate> findById(Long id);

    Optional<Candidate> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Candidate> search(String name, String role, Pageable pageable);
}
