package com.br.ats.consult.api.technology.infrastructure.adapter.in.web;

import com.br.ats.consult.api.technology.domain.model.Technology;
import com.br.ats.consult.api.technology.domain.usecase.CreateTechnologyUseCase;
import com.br.ats.consult.api.technology.domain.usecase.FindTechnologyUseCase;
import com.br.ats.consult.api.technology.infrastructure.adapter.in.web.dto.CreateTechnologyRequest;
import com.br.ats.consult.api.technology.infrastructure.adapter.in.web.dto.TechnologyResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/technologies")
public class TechnologyController {

    private final CreateTechnologyUseCase createTechnologyUseCase;
    private final FindTechnologyUseCase findTechnologyUseCase;

    public TechnologyController(
            CreateTechnologyUseCase createTechnologyUseCase,
            FindTechnologyUseCase findTechnologyUseCase
    ) {
        this.createTechnologyUseCase = createTechnologyUseCase;
        this.findTechnologyUseCase = findTechnologyUseCase;
    }

    @PostMapping
    public ResponseEntity<TechnologyResponse> create(@RequestBody @Valid CreateTechnologyRequest request) {
        Technology created = createTechnologyUseCase.execute(
                request.name(),
                request.category(),
                request.parentId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TechnologyResponse.fromDomain(created));
    }

    @GetMapping
    public ResponseEntity<List<TechnologyResponse>> findAll() {
        List<TechnologyResponse> list = findTechnologyUseCase.findAll()
                .stream()
                .map(TechnologyResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TechnologyResponse> findById(@PathVariable Long id) {
        Technology technology = findTechnologyUseCase.findById(id); // Lança a exceção 404 automaticamente se não encontrar
        return ResponseEntity.ok(TechnologyResponse.fromDomain(technology)); // Ou a forma que o teu controller usa atualmente
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<TechnologyResponse>> findByParentId(@PathVariable Long parentId) {
        List<TechnologyResponse> children = findTechnologyUseCase.findByParentId(parentId)
                .stream()
                .map(TechnologyResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(children);
    }
}