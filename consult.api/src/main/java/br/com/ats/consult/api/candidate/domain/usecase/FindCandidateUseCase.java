package br.com.ats.consult.api.candidate.domain.usecase;

import br.com.ats.consult.api.candidate.domain.model.Candidate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface FindCandidateUseCase {

    Optional<Candidate> findById(Long id);

    Page<Candidate> search(String name, String role, Pageable pageable);
}
