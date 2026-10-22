package com.br.ats.consult.api.client.infrastructure.adapter.in.web;

import com.br.ats.consult.api.client.domain.exception.ClientNotFoundException;
import com.br.ats.consult.api.client.domain.model.Client;
import com.br.ats.consult.api.client.domain.usecase.CreateClientUseCase;
import com.br.ats.consult.api.client.domain.usecase.FindClientUseCase;
import com.br.ats.consult.api.client.infrastructure.adapter.in.web.dto.ClientResponse;
import com.br.ats.consult.api.client.infrastructure.adapter.in.web.dto.CreateClientRequest;
import com.br.ats.consult.api.client.infrastructure.adapter.in.web.mapper.ClientWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
public class ClientController {

    private final CreateClientUseCase createClientUseCase;
    private final FindClientUseCase findClientUseCase;
    private final ClientWebMapper webMapper;

    public ClientController(CreateClientUseCase createClientUseCase, FindClientUseCase findClientUseCase,
                            ClientWebMapper webMapper) {
        this.createClientUseCase = createClientUseCase;
        this.findClientUseCase = findClientUseCase;
        this.webMapper = webMapper;
    }

    @PostMapping
    public ResponseEntity<ClientResponse> create(@RequestBody @Valid CreateClientRequest request) {
        Client created = createClientUseCase.execute(webMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ClientResponse.fromDomain(created));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> findAll() {
        List<ClientResponse> clients = findClientUseCase.findAll().stream()
                .map(ClientResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> findById(@PathVariable Long id) {
        Client client = findClientUseCase.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
        return ResponseEntity.ok(ClientResponse.fromDomain(client));
    }
}
