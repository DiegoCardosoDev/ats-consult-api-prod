package com.br.ats.consult.api.client.infrastructure.persistence.repository;

import com.br.ats.consult.api.client.infrastructure.persistence.entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataClientRepository extends JpaRepository<ClientEntity, Long> {

    Optional<ClientEntity> findByCode(String code);

    List<ClientEntity> findAllByOrderByNameAsc();

    boolean existsByCnpj(String cnpj);

    boolean existsByCode(String code);
}
